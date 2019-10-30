package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年01月15日
 */
@Builder
@Data
public class MemberSecurity {
    private String username;
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private BooleanEnum realVerified;
    private BooleanEnum emailVerified;
    private BooleanEnum phoneVerified;
    private BooleanEnum loginVerified;
    private BooleanEnum fundsVerified;
    private BooleanEnum realAuditing;
    private String mobilePhone;
    private String email;
    private String realName;
    private String realNameRejectReason;
    private String idCard;
    private String avatar;
    private BooleanEnum accountVerified;
    private Integer googleStatus;
    private int transactions;
    private Date transactionTime; //首次交易时间
    private Integer level;

}
