package com.spark.bitrade.entity;

import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.*;

/**
 * 理财币种
 */
@Data
@Entity
public class FinanceCoin {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @JoinColumn(name = "coin_id")
    @ManyToOne
    private Coin coin;

    private CommonStatus status = CommonStatus.NORMAL;
}
