package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@AllArgsConstructor
@Getter
public enum ActivityRewardType implements BaseEnum {
    /**
     * 注册奖励
     */
    REGISTER("注册奖励"),
    /**
     * 交易奖励
     */
    TRANSACTION("交易奖励"),
    /**
    /**
     * 充值奖励
     */
    RECHARGE("充值奖励");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
