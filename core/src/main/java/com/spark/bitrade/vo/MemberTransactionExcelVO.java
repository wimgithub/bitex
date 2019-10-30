package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @auther Cain
 * @date 2018/10/26
 * @time 11:08
 */
@Data
public class MemberTransactionExcelVO {
    @Excel(name = "交易记录编号")
    private Long id;
    @Excel(name = "会员id")
    private Long memberId;
    @Excel(name="会员昵称")
    private String userName;
    @Excel(name = "交易金额")
    private BigDecimal amount;
    @Excel(name = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @Excel(name = "交易类型")
    private String typeCN;
}
