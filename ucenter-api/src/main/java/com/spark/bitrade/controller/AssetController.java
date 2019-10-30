package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.system.CoinExchangeFactory;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.sparkframework.lang.Convert;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@RequestMapping("/asset")
@Slf4j
public class AssetController {
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private LockPositionRecordService lockPositionRecordService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LocaleMessageSourceService sourceService;

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("wallet")
    public MessageResult findWallet(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<MemberWallet> wallets = walletService.findAllByMemberId(member.getId());
        wallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            if (rate != null) {
                wallet.getCoin().setUsdRate(rate.getUsdRate());
                wallet.getCoin().setCnyRate(rate.getCnyRate());
                wallet.getCoin().setSgdRate(rate.getSgdRate());
            } else {
                log.info("unit = {} , rate = null ", wallet.getCoin().getUnit());
            }
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(wallets);
        return mr;
    }

    /**
     * 查询特定类型的记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @param type
     * @return
     */
    @RequestMapping("transaction")
    public Page<MemberTransaction> findTransaction(@SessionAttribute(SESSION_MEMBER) AuthMember member, int pageNo, int pageSize, TransactionType type,String unit) {
        return transactionService.queryByMember(member.getId(), pageNo, pageSize, type,unit);
    }

    /**
     * 查询所有记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("transaction/all")
    public Page<MemberTransaction> findTransaction(
            @SessionAttribute(SESSION_MEMBER) AuthMember member,
            HttpServletRequest request,
            int pageNo,
            int pageSize,
            String symbol) throws ParseException {
        TransactionType type = null;
        if (request.getParameter("type") != null) {
            type = TransactionType.valueOfOrdinal(Convert.strToInt(request.getParameter("type"), 0));
        }
        String startDate = "";
        String endDate = "";
        if (request.getParameter("dateRange") != null) {
            String[] parts = request.getParameter("dateRange").split("~");
            startDate = parts[0].trim();
            endDate = parts[1].trim();
        }
        return transactionService.queryByMember(member.getId(), pageNo, pageSize, type, startDate, endDate,symbol);
    }

    @RequestMapping("wallet/{symbol}")
    public MessageResult findWalletBySymbol(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String symbol) {
        MessageResult mr = MessageResult.success("success");
        mr.setData(walletService.findByCoinUnitAndMemberId(symbol, member.getId()));
        return mr;
    }

    @RequestMapping("wallet/reset-address")
    public MessageResult resetWalletAddress(@SessionAttribute(SESSION_MEMBER) AuthMember member, String unit) {
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            kafkaTemplate.send("reset-member-address", unit, json.toJSONString());
            return MessageResult.success("提交成功");
        } catch (Exception e) {
            return MessageResult.error("未知异常");
        }
    }

    @PostMapping("lock-position")
    public MessageResult lockPositionRecordList(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                @RequestParam(value = "status",required = false) CommonStatus status,
                                                @RequestParam(value = "coinUnit",required = false)String coinUnit,
                                                PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(status!=null){
            booleanExpressions.add(QLockPositionRecord.lockPositionRecord.status.eq(status));
        }
        if(coinUnit!=null){
            Coin coin=coinService.findByUnit(coinUnit);
            if(coin==null){
                return MessageResult.error(sourceService.getMessage("COIN_ILLEGAL"));
            }
            booleanExpressions.add(QLockPositionRecord.lockPositionRecord.coin.eq(coin));
        }
        booleanExpressions.add(QLockPositionRecord.lockPositionRecord.memberId.eq(member.getId()));
        Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
        Page<LockPositionRecord> lockPositionRecordList=lockPositionRecordService.findAll(predicate,pageModel);
        MessageResult result=MessageResult.success();
        result.setData(lockPositionRecordList);
        return result;
    }
}
