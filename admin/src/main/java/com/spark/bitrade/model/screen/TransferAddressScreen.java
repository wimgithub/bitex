package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

@Data
public class TransferAddressScreen {
    private CommonStatus start ;
    private String address;
    private String unit;
}
