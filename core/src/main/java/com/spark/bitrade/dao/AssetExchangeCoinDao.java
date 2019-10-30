package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.AssetExchangeCoin;

public interface AssetExchangeCoinDao extends BaseDao<AssetExchangeCoin> {

    AssetExchangeCoin findAssetExchangeCoinByFromUnitAndToUnit(String fromUnit, String toUnit);
}
