package com.spark.bitrade.service;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.FinanceCoinDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.FinanceCoin;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceCoinService extends BaseService {

    @Autowired
    private FinanceCoinDao financeCoinDao;

    public FinanceCoin save(FinanceCoin financeCoin) {
        return financeCoinDao.save(financeCoin);
    }

    public FinanceCoin findOne(Long financeCoinId) {
        return financeCoinDao.findOne(financeCoinId);
    }

    public List<FinanceCoin> findByCoin(Coin coin) {
        return financeCoinDao.findAllByCoin(coin);
    }

    public List<FinanceCoin> getAllCoins() {
        return financeCoinDao.findAll();
    }

    public List<FinanceCoin> getAllAvailableCoins() {
        Criteria<FinanceCoin> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("status", CommonStatus.NORMAL,false));

        return financeCoinDao.findAll(criteria);
    }
}
