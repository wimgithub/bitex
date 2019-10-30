package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 借款配置
 */
@Data
@Entity
public class DebitConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private Integer type;   //配置类型,0-借贷币种，压币币种,1-借贷时长,2-服务费率，日息费率，抵押费率, 3-出款账户ID

    private String content; //配置内容，json串

    /**
     * type = 0
     * {
     *     "coin":"USDT",
     *     "frozenCoin":"BTC;ETH"
     * }
     *
     * type = 1
     * {
     *     "min":7,
     *     "max":360
     * }
     *
     * type = 2
     * {
     *     "serviceRate":0.01,
     *     "feeRate":0.002,
     *     "mortgageRate":0.6
     * }
     *
     * type = 3
     * {
     *     "mainAccount":330
     * }
     */
}
