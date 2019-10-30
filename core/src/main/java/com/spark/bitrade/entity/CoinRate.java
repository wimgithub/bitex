package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
public class CoinRate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private String baseCoin;

    private String coin;

    private BigDecimal rate;
}
