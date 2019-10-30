package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType implements BaseEnum {
    RECHARGE("充值"),//0
    WITHDRAW("提现"),//1
    TRANSFER_ACCOUNTS("转账"),//2
    EXCHANGE("币币交易"),//3
    OTC_BUY("法币买入"),//4
    OTC_SELL("法币卖出"),//5
    ACTIVITY_AWARD("活动奖励"),//6
    PROMOTION_AWARD("推广奖励"),//7
    DIVIDEND("分红"),//8
    VOTE("投票"),//9
    ADMIN_RECHARGE("人工充值"),//10
    MATCH("配对"),//11
    DEPOSIT("缴纳商家认证保证金"),//12
    GET_BACK_DEPOSIT("退回商家认证保证金"),//13
    LEGAL_RECHARGE("法币充值"),//14
    ASSET_EXCHANGE("币币兑换"),//15
    CHANNEL_AWARD("渠道推广"),//16
    TRANSFER_INTO_LEVER("划转入杠杆钱包"),//17
    TRANSFER_OUT_LEVER("从杠杆钱包划转出"),//18
    MANUAL_AIRDROP("钱包空投"),//19
    LOCK_POSITION("锁仓"),//20
    UNLOCK_POSITION("解锁"),//21
    THIRD_PARTY_TRANSFER("第三方转入"),//22
    THIRD_PARTY_TURN_OUT("第三方转出"),//23
    FINANCE_TIME_INCOME("活期收益"),//24
    FINANCE_FIXED_INCOME("定期收益"),//25
    FINANCE_PAYMENT("收付款"),//26
    DEBIT_IN("借款"),//27
    DEBIT_OUT("出款"),//28
    REPAY_OUT("还款"),//29
    REPAY_IN("收款"),//30
    INTEREST_OUT("付息"),//31
    INTEREST_IN("收息"),//32
    FINANCE_TIME_BREAK("活期解决扣除本金");//33

    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    public static TransactionType valueOfOrdinal(int ordinal){
        for(TransactionType type: TransactionType.values()){
            if(type.getOrdinal()==ordinal){
                return type;
            }
        }
        return null;
    }
}
