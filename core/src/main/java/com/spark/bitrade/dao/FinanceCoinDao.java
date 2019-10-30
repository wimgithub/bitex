package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.FinanceCoin;

import java.util.List;

public interface FinanceCoinDao extends BaseDao<FinanceCoin> {

    List<FinanceCoin> findAllByCoin(Coin coin);
}
