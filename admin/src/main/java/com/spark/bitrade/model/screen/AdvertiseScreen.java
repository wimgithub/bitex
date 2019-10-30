package com.spark.bitrade.model.screen;

import com.spark.bitrade.constant.AdvertiseControlStatus;
import com.spark.bitrade.constant.AdvertiseType;
import lombok.Data;

@Data
public class AdvertiseScreen extends AccountScreen{

    AdvertiseType advertiseType;

    String payModel ;

    /**
     * 广告状态 (012  上架/下架/关闭)
     */
    AdvertiseControlStatus status ;

}
