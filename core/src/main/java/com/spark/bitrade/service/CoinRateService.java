package com.spark.bitrade.service;

import com.spark.bitrade.dao.CoinRateDao;
import com.spark.bitrade.entity.CoinRate;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CoinRateService extends BaseService {

    @Autowired
    private CoinRateDao coinRateDao;

    public CoinRate save(CoinRate coinRate) {
        return coinRateDao.saveAndFlush(coinRate);
    }

    public CoinRate findOne(Long id) {
        return coinRateDao.findOne(id);
    }

    public List<CoinRate> getAll() {
        return coinRateDao.findAll();
    }

    public BigDecimal getRate(String baseCoin, String coin) {
        CoinRate coinRate = coinRateDao.findCoinRateByBaseCoinAndCoin(baseCoin,coin);
        return coinRate.getRate();
    }
}
