package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 币种汇率
 */
@Data
@Entity
public class AssetExchangeCoin {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Column(columnDefinition = "decimal(18,2) comment '汇率'")
    private BigDecimal exchangeRate;

    private String fromUnit;

    private String toUnit;
}
