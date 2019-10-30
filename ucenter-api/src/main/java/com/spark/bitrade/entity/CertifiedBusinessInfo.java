package com.spark.bitrade.entity;

import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Data;

/**
 * @author Zhang Jinwei
 * @date 2018年02月26日
 */
@Data
public class CertifiedBusinessInfo {
    private MemberLevelEnum memberLevel;
    private CertifiedBusinessStatus certifiedBusinessStatus;
    private String email;
    /**
     * * 审核失败原因
     */
    private String detail;
    /**
     *
     * 退保原因
     */
    private String reason ;
}
