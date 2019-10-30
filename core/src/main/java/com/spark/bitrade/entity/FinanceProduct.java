package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 理财产品表
 */
@Data
@Entity
public class FinanceProduct {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    /**
     * 下架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    private String productName;     //产品名称

    private String productExplain;  //产品简介

    private String productLabel;    //产品标签

    private String coinName;        //币种单位,多个分号隔开

    private Integer type;           //类型，0-活期,1-定期

    private BigDecimal totalAmount; //所需总额,定期为每份额度

    private String minAmount;       //用户最小投资,活期根据coinName分号隔开,定期为最小购买份数

    private String maxAmount;       //用户最大投资,活期根据coinName分号隔开

    private BigDecimal timeRate;    //活期日利率，可能每天改动

    private String fixedRate;       //定期月利率, (0.001;0.002;0.003;0.004)

    private String fixedBreakRate; //定期解约月利率, 活期: 天数;小于扣本金利率;大于扣本金利率, 30;0.05;0.01

    private String period;          //定期：周期，月数（3个月，6个月，12个月等）(1;3;6;12)

    private Integer status=0;       //状态,0-未开始，1-进行中，2-已结束

    private String note;            //说明
}