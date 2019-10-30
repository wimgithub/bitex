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
public enum RewardRecordType implements BaseEnum {
    /**
     * 推广
     */
    PROMOTION("推广"),
    /**
     * 活动
     */
    ACTIVITY("活动"),
    /**
     * 分红
     */
    DIVIDEND("分红");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
