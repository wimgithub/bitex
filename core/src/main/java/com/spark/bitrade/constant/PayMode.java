package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年01月20日
 */
@AllArgsConstructor
@Getter
public enum PayMode implements BaseEnum {
    ALIPAY("支付宝"), WECHAT("微信"), BANK("银联"),PAYPAL("Paypal");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
