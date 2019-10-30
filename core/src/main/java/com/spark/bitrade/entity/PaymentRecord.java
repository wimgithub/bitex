package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收付明细表
 */
@Data
@Entity
public class PaymentRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long fromMemberId;

    private Long toMemberId;

    private String symbol;

    private BigDecimal amount;

    private String memo;
}
