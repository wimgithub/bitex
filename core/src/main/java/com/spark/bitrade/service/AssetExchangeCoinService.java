package com.spark.bitrade.service;

import com.spark.bitrade.dao.AssetExchangeCoinDao;
import com.spark.bitrade.entity.AssetExchangeCoin;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetExchangeCoinService extends BaseService {

    @Autowired
    private AssetExchangeCoinDao assetExchangeCoinDao;

    public AssetExchangeCoin save(AssetExchangeCoin assetExchangeCoin) {
        return assetExchangeCoinDao.saveAndFlush(assetExchangeCoin);
    }

    public AssetExchangeCoin findByFromAndToUnit(String fromUnit, String toUnit) {
        return assetExchangeCoinDao.findAssetExchangeCoinByFromUnitAndToUnit(fromUnit, toUnit);
    }

}
