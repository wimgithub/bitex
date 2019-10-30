package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 利息明细
 */
@Data
@Entity
public class DebitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JoinColumn(name = "debit_detail_id")
    @ManyToOne
    private DebitDetail debitDetail;

    private BigDecimal amount;

    private int type;   //0-借款数额  正数， 1-服务费  负数，2-日息  负数, 3-还款数额  负数
}
