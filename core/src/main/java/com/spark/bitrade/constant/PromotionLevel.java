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
public enum PromotionLevel implements BaseEnum {
    /**
     * 一级
     */
    ONE("一级"),
    /**
     * 二级
     */
    TWO("二级"),
    /**
     * 三级
     */
    THREE("三级");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
