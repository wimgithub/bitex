package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收益记录
 */
@Entity
@Data
public class FinanceRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JoinColumn(name = "finance_product_detail_id")
    @ManyToOne
    private FinanceProductDetail financeProductDetail;

    /**
     * 收益
     */
    private BigDecimal amount;

    /**
     * 利率
     */
    private BigDecimal rate; //对应类型的利率

    private Integer type; //0-活期，1-定期，2-定期，提前结束, 3-活期，提前结束，扣除本金数量，负数

    @Column(columnDefinition = "decimal(18,2) comment '汇率'")
    private BigDecimal exchangeRate; //对应币种汇率
}
