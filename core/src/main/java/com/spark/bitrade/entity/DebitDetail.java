package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 借款详细
 */
@Data
@Entity
public class DebitDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;        //借款时间

    @JoinColumn(name = "member_id")
    @ManyToOne
    private Member member;          //借款人

    private String coin;            //借款币种

    private BigDecimal amount;      //借款金额

    private String frozenCoin;      //抵押币种

    private BigDecimal frozenAmount;//抵押数量

    private int days;               //借款天数

    private BigDecimal serviceRate; //服务费率

    private BigDecimal feeRate;     //利息费率

    private int status = 0;             //借款状态（0-借贷中，1-已完成）
}
