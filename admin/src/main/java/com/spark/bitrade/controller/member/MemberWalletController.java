package com.spark.bitrade.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.dto.MemberWalletDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.MemberWalletScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("member/member-wallet")
@Slf4j
public class MemberWalletController extends BaseAdminController {

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private LockPositionRecordService lockPositionRecordService;
    @Autowired
    private MongoTemplate mongoTemplate ;
    @Autowired
    private ExchangeOrderService exchangeOrderService;


    @RequiresPermissions("member:member-wallet:closeBalance")
    @PostMapping("balance")
    @AccessLog(module = AdminModule.MEMBER, operation = "余额管理")
    public MessageResult getBalance(
            PageModel pageModel,
            MemberWalletScreen screen) {
        QMemberWallet qMemberWallet = QMemberWallet.memberWallet;
        QMember qMember = QMember.member;
        List<Predicate> criteria = new ArrayList<>();
        if (StringUtils.hasText(screen.getAccount()))
            criteria.add(qMember.username.like("%" + screen.getAccount() + "%")
                    .or(qMember.mobilePhone.like(screen.getAccount() + "%"))
                    .or(qMember.email.like(screen.getAccount() + "%"))
                    .or(qMember.realName.like("%" + screen.getAccount() + "%")));
        if (!StringUtils.isEmpty(screen.getWalletAddress()))
            criteria.add(qMemberWallet.address.eq(screen.getWalletAddress()));

        if (!StringUtils.isEmpty(screen.getUnit()))
            criteria.add(qMemberWallet.coin.unit.eq(screen.getUnit()));

        if (screen.getMaxAllBalance() != null)
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).loe(screen.getMaxAllBalance()));

        if (screen.getMinAllBalance() != null)
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).goe(screen.getMinAllBalance()));

        if (screen.getMaxBalance() != null)
            criteria.add(qMemberWallet.balance.loe(screen.getMaxBalance()));

        if (screen.getMinBalance() != null)
            criteria.add(qMemberWallet.balance.goe(screen.getMinBalance()));

        if (screen.getMaxFrozenBalance() != null)
            criteria.add(qMemberWallet.frozenBalance.loe(screen.getMaxFrozenBalance()));

        if (screen.getMinFrozenBalance() != null)
            criteria.add(qMemberWallet.frozenBalance.goe(screen.getMinFrozenBalance()));

        Page<MemberWalletDTO> page = memberWalletService.joinFind(criteria, qMember, qMemberWallet, pageModel);
        return success(messageSource.getMessage("SUCCESS"), page);
    }

    @RequiresPermissions("member:member-wallet:recharge")
    @PostMapping("recharge")
    @AccessLog(module = AdminModule.MEMBER, operation = "充币管理")
    public MessageResult recharge(
            @RequestParam("unit") String unit,
            @RequestParam("uid") Long uid,
            @RequestParam("amount") BigDecimal amount) {
        Coin coin = coinService.findByUnit(unit);
        if (coin == null) {
            return error("币种不存在");
        }
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, uid);
        Assert.notNull(memberWallet, "wallet null");
        memberWallet.setBalance(memberWallet.getBalance().add(amount));

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(memberWallet.getMemberId());
        memberTransaction.setSymbol(unit);
        memberTransaction.setType(TransactionType.ADMIN_RECHARGE);
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransactionService.save(memberTransaction);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:member-wallet:reset-address")
    @PostMapping("reset-address")
    @AccessLog(module = AdminModule.MEMBER, operation = "重置钱包地址")
    public MessageResult resetAddress(String unit, long uid) {
        Member member = memberService.findOne(uid);
        Assert.notNull(member, "member null");
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            log.info("kafkaTemplate send : topic = {reset-member-address} , unit = {} , uid = {}", unit, json);
            kafkaTemplate.send("reset-member-address", unit, json.toJSONString());
            return MessageResult.success(messageSource.getMessage("SUCCESS"));
        } catch (Exception e) {
            return MessageResult.error(messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:member-wallet:lock-wallet")
    @PostMapping("lock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "锁定钱包")
    public MessageResult lockWallet(Long uid, String unit) {
        if (memberWalletService.lockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:member-wallet:unlock-wallet")
    @PostMapping("unlock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "解锁钱包")
    public MessageResult unlockWallet(Long uid, String unit) {
        if (memberWalletService.unlockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:member-wallet:lock-wallet")
    @PostMapping("lock-position")
    @AccessLog(module = AdminModule.MEMBER, operation = "锁仓")
    public MessageResult lockPosition(Long uid, String unit, BigDecimal amount, String reason, Date unlockTime) {
        if(uid==null||StringUtils.isEmpty(unit)||amount==null||amount.compareTo(BigDecimal.ZERO)<=0){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        Member member=memberService.findOne(uid);
        if(member==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        Coin coin=coinService.findByUnit(unit);
        if(coin==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        MemberWallet memberWallet=memberWalletService.findByCoinAndMember(coin,member);
        if(memberWallet==null){
            return MessageResult.error(messageSource.getMessage("WALLET_NOT_FOUND"));
        }
        if(memberWallet.getBalance().compareTo(amount)<0){
            return MessageResult.error(messageSource.getMessage("BALANCE_NOT_ENOUGH"));
        }
        return lockPositionRecordService.lockPosition(memberWallet,amount,member,reason,unlockTime);
    }

    @RequiresPermissions("member:member-wallet:unlock-wallet")
    @PostMapping("unlock-position")
    @AccessLog(module = AdminModule.MEMBER, operation = "解锁锁仓金额")
    public MessageResult unlockPosition(@RequestParam("lockPositionId") Long lockPositionId) {
        LockPositionRecord lockPositionRecord=lockPositionRecordService.findById(lockPositionId);
        if(lockPositionRecord==null){
            return MessageResult.error(messageSource.getMessage("Incorrect_Parameters"));
        }
        return lockPositionRecordService.unlock(lockPositionRecord);
    }

    @PostMapping("countFrozen")
    public MessageResult countFrozen(){
        new Thread(){
            public void run(){
                frozenMemberWallet();
            }
        }.start();
        return MessageResult.success();
    }

    public void frozenMemberWallet(){
        log.info("======================任务开始=========================");
        //人工确认已经停止交易、订单都已经处于完成或取消状态后，查询冻结金额大于0的钱包
        List<MemberWallet> memberWalletList=memberWalletService.findByFrozenBalanceBiggerThenZero();
        log.info("memberWalletList.size={}",memberWalletList.size());
        if(memberWalletList!=null&&memberWalletList.size()>0){
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd HHmmss");
            String fileName=sdf.format(new Date())+"-log.txt";
            fileName=fileName.replace(" ","-");
            File file=new File("/web/log/"+fileName);
            //File file=new File("E:\\workspace\\wallet-order-log\\"+fileName);
            for(MemberWallet memberWallet:memberWalletList){
                check(memberWallet,file);
            }
            log.info("==========================任务结束===========================");
        }
    }

    private void check(MemberWallet memberWallet,File file){
        log.info("memberWallet={}",memberWallet.toString());
        List<ExchangeOrder> buyOrderList=exchangeOrderService.findAllCompletedOrderByMemberIdAndCoinAndDirection(memberWallet.getMemberId(),memberWallet.getCoin().getUnit(),ExchangeOrderDirection.BUY);
        List<ExchangeOrder> sellOrderList=exchangeOrderService.findAllCompletedOrderByMemberIdAndCoinAndDirection(memberWallet.getMemberId(),memberWallet.getCoin().getUnit(),ExchangeOrderDirection.SELL);
        log.info("buyOrderList.size={}",buyOrderList.size());
        log.info("sellOrderList.size={}",sellOrderList.size());
        buyOrderList.addAll(sellOrderList);
        List<ExchangeOrder> exchangeOrderList=buyOrderList;
        if(exchangeOrderList!=null&&exchangeOrderList.size()>0){
            PrintWriter writer=null;
            try {
                writer=new PrintWriter(new FileWriter(file,true));
                writer.write("memberWalletId="+memberWallet.getId()+",orderList.size="+exchangeOrderList.size()+"\n");
                for(int i=0;i<exchangeOrderList.size();i+=5000){
                    int end;
                    if(i+5000<exchangeOrderList.size()){
                        end=i+5000;
                    }else{
                        end=exchangeOrderList.size();
                    }
                    made(i,end,exchangeOrderList,memberWallet,writer);
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(writer!=null){
                    writer.flush();
                    writer.close();
                }
            }
        }
    }

    private void made(int start,int end,List<ExchangeOrder> exchangeOrderList,MemberWallet memberWallet,PrintWriter writer){
        log.info("memberWalletId={},start={},end={}",memberWallet.getId(),start,end);
        List<String> orderIdList=new ArrayList<>();
        for(int i=start;i<end;i++){
            orderIdList.add(exchangeOrderList.get(i).getOrderId());
        }
        Query query = new Query(Criteria.where("orderId").in(orderIdList));
        List<OrderDetailAggregation> list = mongoTemplate.find(query,OrderDetailAggregation.class,"order_detail_aggregation");
        log.info("list.size={}",list.size());
        for(int i=start;i<end;i++){
            ExchangeOrder exchangeOrder=exchangeOrderList.get(i);
            //找到冻结币种为基币的买单和冻结币种为交易币的卖单
            if((memberWallet.getCoin().getUnit().equalsIgnoreCase(exchangeOrder.getBaseSymbol())
                    &&exchangeOrder.getDirection().equals(ExchangeOrderDirection.BUY))
                    ||(memberWallet.getCoin().getUnit().equalsIgnoreCase(exchangeOrder.getCoinSymbol())
                    &&exchangeOrder.getDirection().equals(ExchangeOrderDirection.SELL))){
                BigDecimal tradedAmount=BigDecimal.ZERO;
                if(list.size()>0){
                    for(OrderDetailAggregation detailAggregation:list){
                        if(detailAggregation.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())){
                            tradedAmount=tradedAmount.add(new BigDecimal(detailAggregation.getAmount()+detailAggregation.getFee()).setScale(8,BigDecimal.ROUND_HALF_UP));
                        }
                    }
                    tradedAmount=tradedAmount.setScale(8,BigDecimal.ROUND_HALF_UP);
                    BigDecimal orderTradeAmount;
                    if(exchangeOrder.getDirection().equals(ExchangeOrderDirection.SELL)){
                        orderTradeAmount=exchangeOrder.getTurnover();
                    }else{
                        orderTradeAmount=exchangeOrder.getTradedAmount();
                    }
                    if(orderTradeAmount.compareTo(tradedAmount)!=0){
                        BigDecimal sub=orderTradeAmount.subtract(tradedAmount);
                        if(sub.compareTo(BigDecimal.ZERO)<0){
                            sub=sub.negate();
                            writer.write("update member_wallet set balance=balance+"+sub.toPlainString()+" , frozen_balance=frozen_balance-"+sub.toPlainString()+" where id="+memberWallet.getId()+"\n");
                        }else{
                            writer.write("update member_wallet set balance=balance-"+sub.toPlainString()+" , frozen_balance=frozen_balance+"+sub.toPlainString()+" where id="+memberWallet.getId()+"\n");
                        }
                        if(exchangeOrder.getDirection().equals(ExchangeOrderDirection.SELL)){
                            writer.write("update exchange_order set turnover="+sub.toPlainString()+" where order_id="+exchangeOrder.getOrderId()+"\n");
                        }else{
                            writer.write("update exchange_order set traded_amount="+sub.toPlainString()+" where order_id="+exchangeOrder.getOrderId()+"\n");
                        }
                        //writer.write("memberWalletId="+memberWallet.getId()+",orderId="+exchangeOrder.getOrderId()+",direction="+exchangeOrder.getDirection().name()+",orderTradeAmount="+orderTradeAmount+",mongoTradedAmount="+tradedAmount+"\n");
                    }
                }
            }
        }
    }
}
