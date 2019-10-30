package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @auther Cain
 * @date 2018/10/17
 * @time 15:20
 */
@Embeddable
@Data
public class BusinessAuthApplyVO {

    private Long id;

    @Excel(name="会员昵称")
    private String userName;

    @Excel(name="邮箱")
    private String email;

    @Excel(name="真实姓名")
    private String realName;

    @Excel(name="手机号")
    private String mobilePhone;

    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum memberLevel;

    @Excel(name="会员等级")
    private String memberLevelCN;

    /**
     * 申请时间
     */
    @Excel(name="申请时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 审核时间
     */
    @Excel(name="审核时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditingTime;

    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum publishAdvertise;

    @Excel(name="是否允许发布广告")
    private String publishAdvertiseCN;
    /**
     * 认证商家状态
     */
    @Enumerated(value = EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus;

    @Excel(name="审核状态")
    private String certifiedBusinessStatusCN;
}
