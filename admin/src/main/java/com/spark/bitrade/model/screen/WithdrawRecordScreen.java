package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Data;

@Data
public class WithdrawRecordScreen extends AccountScreen{

    private String unit ;

    /**
     * 提现地址
     */
    private String address ;

    private WithdrawStatus status ;

    /**
     * 是否自动提现
     */
    private BooleanEnum isAuto;

    private Long memberId ;

    private String orderSn;

    private Long withdrawRecordId;

    private String memberUsername;

    private String memberRealName;

    private String phone;
}
