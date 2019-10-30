package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.CoinRate;

public interface CoinRateDao extends BaseDao<CoinRate> {

    CoinRate findCoinRateByBaseCoinAndCoin(String baseCoin, String coin);
}
