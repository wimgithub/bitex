package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import lombok.Data;

@Data
public class SysAdvertiseScreen {
    private String serialNumber;
    private SysAdvertiseLocation sysAdvertiseLocation;
    private CommonStatus status;
}
