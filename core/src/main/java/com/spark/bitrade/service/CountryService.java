package com.spark.bitrade.service;

import com.spark.bitrade.dao.CountryDao;
import com.spark.bitrade.entity.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年02月10日
 */
@Service
public class CountryService {
    @Autowired
    private CountryDao countryDao;

    public List<Country> getAllCountry(){
        return countryDao.findAllOrderBySort();
    }

    public Country findOne(String zhName){
        return countryDao.findByZhName(zhName);
    }

    public List<Country> findByLegalCurrency(String legalCurrency){
        return countryDao.findByLocalCurrency(legalCurrency);
    }

}
