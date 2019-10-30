package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年04月24日
 */
@AllArgsConstructor
@Getter
public enum Platform implements BaseEnum {
    ANDROID("安卓"), IOS("苹果");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
