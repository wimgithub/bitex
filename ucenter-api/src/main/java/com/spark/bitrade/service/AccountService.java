package com.spark.bitrade.service;

import com.spark.bitrade.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


/**
 * @auther Cain
 * @date 2018/9/27
 * @time 18:08
 */
@Service
public class AccountService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public String getCollectionName(String coinUnit){
        return coinUnit + "_address_book";
    }

    public void save(Account account,String coinUnit){
        mongoTemplate.insert(account,getCollectionName(coinUnit));
    }

    /**
     * 根据账户名查找
     * @param coinUnit
     * @param username
     * @return
     */
    public Account findByName(String coinUnit,String username){
        Query query = new Query();
        Criteria criteria = Criteria.where("account").is(username);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query,Account.class,getCollectionName(coinUnit));
    }

    /**
     * 根据地址查找
     * @param address
     * @return
     */
    public Account findByAddress(String address,String coinUnit){
        Query query = new Query();
        Criteria criteria = Criteria.where("address").is(address);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query,Account.class,getCollectionName(coinUnit));
    }

    public void saveOne(String username, String fileName, String address,String coinUnit) {
        Account account = new Account();
        account.setAccount(username);
        account.setAddress(address.toLowerCase());
        account.setWalletFile(fileName);
        save(account,coinUnit);
    }
}