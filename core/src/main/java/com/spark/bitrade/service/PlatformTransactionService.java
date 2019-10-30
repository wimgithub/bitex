package com.spark.bitrade.service;

import com.spark.bitrade.dao.PlatformTransactionDao;
import com.spark.bitrade.entity.PlatformTransaction;
import com.spark.bitrade.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlatformTransactionService {
    @Autowired
    private PlatformTransactionDao platformTransactionDao;

    public void add(BigDecimal amount,int direction,int type,String bizOrderId){
        PlatformTransaction transaction = new PlatformTransaction();
        transaction.setAmount(amount);
        transaction.setDirection(direction);
        transaction.setType(type);
        transaction.setBizOrderId(bizOrderId);
        transaction.setDay(DateUtil.getDate());
        transaction.setTime(DateUtil.getCurrentDate());
        save(transaction);
    }

    public void save(PlatformTransaction transaction){
        platformTransactionDao.save(transaction);
    }
}
