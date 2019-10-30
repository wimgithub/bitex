package com.spark.bitrade.service;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.CoinDao;
import com.spark.bitrade.dao.TransferAddressDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.TransferAddress;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年02月27日
 */
@Service
public class TransferAddressService extends TopBaseService<TransferAddress,TransferAddressDao> {

    @Autowired
    public void setDao(TransferAddressDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private CoinDao coinDao;

    public List<TransferAddress> findByUnit(String unit){
        Coin coin = coinDao.findByUnit(unit);
        return dao.findAllByStatusAndCoin(CommonStatus.NORMAL, coin);
    }
    public List<TransferAddress> findByCoin(Coin coin){
        return dao.findAllByStatusAndCoin(CommonStatus.NORMAL, coin);
    }

    public TransferAddress findOnlyOne(Coin coin,String address){
        return dao.findByAddressAndCoin(address, coin);
    }

}
