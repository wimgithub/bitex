package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.TransferAddress;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年02月27日
 */
public interface TransferAddressDao extends BaseDao<TransferAddress> {
    List<TransferAddress> findAllByStatusAndCoin(CommonStatus status, Coin coin);

    TransferAddress findByAddressAndCoin(String address, Coin coin);
}
