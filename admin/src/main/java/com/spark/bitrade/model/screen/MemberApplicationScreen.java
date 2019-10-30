package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.AuditStatus;
import lombok.Data;

import java.util.Date;

@Data
public class MemberApplicationScreen extends AccountScreen{
    private AuditStatus auditStatus;//审核状态
    private String cardNo ; //身份证号
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date approveStartTime;//发起申请的时间段，起始
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date approveEndTime;//发起申请的时间段，结束
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date auditStartTime;//审核时间段，起始
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date auditEndTime;//审核时间段，结束
}
