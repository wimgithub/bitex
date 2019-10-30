package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.sparkframework.sql.DataException;
import com.spark.bitrade.util.PredicateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 委托订单处理类
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 添加委托订单
     *
     * @return
     */
    @RequestMapping("add")
    public MessageResult addOrder(
            @SessionAttribute(SESSION_MEMBER) AuthMember authMember,
            ExchangeOrderDirection direction,
            String symbol,
            BigDecimal price,
            BigDecimal amount,
            ExchangeOrderType type) throws SQLException, DataException {
        Long startTime=new Date().getTime();
        if(direction == null || type == null){
            return MessageResult.error(500,msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        Member member=memberService.findOne(authMember.getId());
        if(member.getMemberLevel()== MemberLevelEnum.GENERAL){
            return MessageResult.error(500,"请先进行实名认证");
        }
        //是否被禁止交易
        if(member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)){
            return MessageResult.error(500,msService.getMessage("CANNOT_TRADE"));
        }
        ExchangeOrder order = new ExchangeOrder();
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        if (exchangeCoin == null || exchangeCoin.getEnable() != 1) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        String baseCoin = exchangeCoin.getBaseSymbol();
        String exCoin = exchangeCoin.getCoinSymbol();
        Coin coin;
        if (direction == ExchangeOrderDirection.SELL) {
            coin = coinService.findByUnit(exCoin);
        } else {
            coin = coinService.findByUnit(baseCoin);
        }
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        //设置价格精度
        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
        //委托数量和精度控制
        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
            //最小成交额控制
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                return MessageResult.error(500, "成交额至少为" + exchangeCoin.getMinTurnover());
            }
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            //成交量范围控制
            if(exchangeCoin.getMaxVolume()!=null&&exchangeCoin.getMaxVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMaxVolume().compareTo(amount)<0){
                return MessageResult.error(msService.getMessage("AMOUNT_OVER_SIZE")+" "+exchangeCoin.getMaxVolume());
            }
            if(exchangeCoin.getMinVolume()!=null&&exchangeCoin.getMinVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMinVolume().compareTo(amount)>0){
                return MessageResult.error(msService.getMessage("AMOUNT_TOO_SMALL")+" "+exchangeCoin.getMinVolume());
            }
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }
        MemberWallet baseCoinWallet = walletService.findByCoinUnitAndMemberId(baseCoin, member.getId());
        MemberWallet exCoinWallet = walletService.findByCoinUnitAndMemberId(exCoin, member.getId());
        if (baseCoinWallet == null || exCoinWallet == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        if (baseCoinWallet.getIsLock() == BooleanEnum.IS_TRUE || exCoinWallet.getIsLock() == BooleanEnum.IS_TRUE) {
            return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
        }
        //如果有最低卖价限制，出价不能低于此价,且禁止市场价格卖
        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        //查看是否启用市价买卖
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.BUY) {
                return MessageResult.error(500, "不支持市价购买");
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.SELL) {
                return MessageResult.error(500, "不支持市价出售");
            }
        }
        //限制委托数量
        if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(member.getId(), symbol, direction) >= exchangeCoin.getMaxTradingOrder()) {
            return MessageResult.error(500, "超过最大挂单数量 " + exchangeCoin.getMaxTradingOrder());
        }
        order.setMemberId(member.getId());
        order.setSymbol(symbol);
        order.setBaseSymbol(baseCoin);
        order.setCoinSymbol(exCoin);
        order.setType(type);
        order.setDirection(direction);
        if(order.getType() == ExchangeOrderType.MARKET_PRICE){
            order.setPrice(BigDecimal.ZERO);
        }
        else{
            order.setPrice(price);
        }
        order.setMarginTrade(BooleanEnum.IS_FALSE);
        //限价买入单时amount为用户设置的总成交额
        order.setAmount(amount);

        MessageResult mr = orderService.addOrder(member.getId(), order);
        if (mr.getCode() != 0) {
            return MessageResult.error(500, "提交订单失败:" + mr.getMessage());
        }
        // 发送消息至Exchange系统
        kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(order));
        MessageResult result = MessageResult.success(msService.getMessage("SUCCESS"));
        result.setData(order.getOrderId());
        Long usedTime=new Date().getTime()-startTime;
        log.info("==========添加订单耗时:{}===========",usedTime);
        return result;
    }


    /**
     * 历史委托
     */
    @RequestMapping("history")
    public Page<ExchangeOrder> historyOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize,
                                            @RequestParam(value = "startTime",required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                                            @RequestParam(value = "endTime",required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime) {
        Page<ExchangeOrder> page = orderService.findHistory(member.getId(), symbol, pageNo, pageSize,BooleanEnum.IS_FALSE,startTime,endTime);
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        page.getContent().forEach(exchangeOrder -> {
            exchangeOrder.setPriceStr(exchangeOrder.getPrice().setScale(coin.getBaseCoinScale(), RoundingMode.DOWN).toPlainString());
            exchangeOrder.setAmountStr(exchangeOrder.getAmount().setScale(coin.getCoinScale(),RoundingMode.DOWN).toPlainString());
            //获取交易成交详情
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
        });
        return page;
    }

    /**
     * 当前委托
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("current")
    public Page<ExchangeOrder> currentOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize,
                                            @RequestParam(value = "startTime",required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                                            @RequestParam(value = "endTime",required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime) {
        Page<ExchangeOrder> page = orderService.findCurrent(member.getId(), symbol, pageNo, pageSize,BooleanEnum.IS_FALSE,startTime,endTime);
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        page.getContent().forEach(exchangeOrder -> {
            exchangeOrder.setPriceStr(exchangeOrder.getPrice().setScale(coin.getBaseCoinScale(), RoundingMode.DOWN).toPlainString());
            exchangeOrder.setAmountStr(exchangeOrder.getAmount().setScale(coin.getCoinScale(),RoundingMode.DOWN).toPlainString());
            //获取交易成交详情
            BigDecimal tradedAmount = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
        });
        return page;
    }


    /**
     * 查询委托成交明细
     *
     * @param member
     * @param orderId
     * @return
     */
    @RequestMapping("detail/{orderId}")
    public List<ExchangeOrderDetail> currentOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }

    @RequestMapping("cancel/{orderId}")
    public MessageResult cancelOrder(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) throws Exception {
        ExchangeOrder order = orderService.findOne(orderId);
        if (order.getMemberId() != member.getId()) {
            return MessageResult.error(500, "禁止操作");
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "订单不在交易");
        }
        if(isExchangeOrderExist(order)){
            if (maxCancelTimes > 0 && orderService.findTodayOrderCancelTimes(member.getId(), order.getSymbol()) >= maxCancelTimes) {
                return MessageResult.error(500, "你今天已经取消了 " + maxCancelTimes + " 次");
            }
            // 发送消息至Exchange系统
            kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
        }
        else{
            //强制取消
            orderService.forceCancelOrder(order);
        }
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    /**
     * 批量撤单
     * @param member
     * @param symbol
     * @param type
     * @return
     */
    @PostMapping("batchCancel")
    public MessageResult batchCancelOrder(@SessionAttribute(SESSION_MEMBER)AuthMember member,
                                          @RequestParam(value = "symbol",required = false)String symbol,
                                          @RequestParam(value = "type",required = false)ExchangeOrderType type,
                                          @RequestParam(value = "direction")ExchangeOrderDirection direction,
                                          PageModel pageModel){
        log.info("start batchCancelOrder...");
        ArrayList<BooleanExpression> booleanExpressions=new ArrayList<>();
        booleanExpressions.add(QExchangeOrder.exchangeOrder.memberId.eq(member.getId()));
        booleanExpressions.add(QExchangeOrder.exchangeOrder.status.eq(ExchangeOrderStatus.TRADING));
        if(!StringUtils.isEmpty(symbol)){
            booleanExpressions.add(QExchangeOrder.exchangeOrder.symbol.eq(symbol));
        }
        if(type!=null){
            booleanExpressions.add(QExchangeOrder.exchangeOrder.type.eq(type));
        }
        if(direction!=null){
            booleanExpressions.add(QExchangeOrder.exchangeOrder.direction.eq(direction));
        }
        Predicate predicate= PredicateUtils.getPredicate(booleanExpressions);
        boolean flag=true;
        int pageSize=1000,pageNumber=1;
        pageModel.setPageSize(pageSize);
        while (flag){
            pageModel.setPageNo(pageNumber);
            Page<ExchangeOrder> orders =  orderService.findAll(predicate,pageModel.getPageable(Sort.Direction.ASC));
            List<ExchangeOrder> orderList=orders.getContent();
            if(orderList.isEmpty()){
                flag=false;
                continue;
            }
            for(ExchangeOrder order:orderList){
                // 发送消息至Exchange系统
                kafkaTemplate.send("exchange-order-cancel",order.getSymbol(), JSON.toJSONString(order));
                //log.info("orderId:"+order.getOrderId()+",time:"+order.getTime());
            }
            log.info("pageNumber={},pageSize={}",pageNumber,pageSize);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.info("context",e);
                //e.printStackTrace();
            }
            pageNumber++;
        }
        log.info("end batchCancelOrder...");
        return MessageResult.success();
    }

    /**
     * 查找撮合交易器中订单是否存在
     * @param order
     * @return
     */
    public boolean isExchangeOrderExist(ExchangeOrder order){
        try {
            String serviceName = "service-exchange-trade";
            String url = "http://" + serviceName + "/monitor/order?symbol=" + order.getSymbol() + "&orderId=" + order.getOrderId() + "&direction=" + order.getDirection() + "&type=" + order.getType();
            ResponseEntity<ExchangeOrder> result = restTemplate.getForEntity(url, ExchangeOrder.class);
            return result != null;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
