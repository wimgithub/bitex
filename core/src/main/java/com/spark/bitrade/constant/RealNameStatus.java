package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年02月25日
 */
@AllArgsConstructor
@Getter
public enum RealNameStatus implements BaseEnum {
    /**
     * 未认证
     */
    NOT_CERTIFIED("未认证"),
    /**
     * 审核中
     */
    AUDITING("审核中"),
    /**
     * 已认证
     */
    VERIFIED("已认证");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
