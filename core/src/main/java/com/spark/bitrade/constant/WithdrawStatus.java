package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Zhang Jinwei
 * @date 2018年02月25日
 */
@AllArgsConstructor
@Getter
public enum WithdrawStatus implements BaseEnum {
    PROCESSING("审核中"),WAITING("等待放币"),FAIL("失败"), SUCCESS("成功"),
    TRANSFER("转账中");//异步打钱才有的状态
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
