package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 理财产品购买明细
 */
@Data
@Entity
public class FinanceProductDetail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;   //定期时的到期时间

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member member;

    private String coinName; //币种

    private Integer period; //定期时的周期

    private BigDecimal rate; //定期时的月利率

    private BigDecimal breakRate; //定期时提前结束时的利率

    @JoinColumn(name = "finance_product_id")
    @ManyToOne
    private FinanceProduct financeProduct;

    /**
     * 购买数量
     */
    private BigDecimal amount;

    /**
     * 用户购买产品状态
     * 0：购买---计算收益
     * 1：解约---不计算收益
     * 2：产品到期
     */
    private Integer status = 0;
}
