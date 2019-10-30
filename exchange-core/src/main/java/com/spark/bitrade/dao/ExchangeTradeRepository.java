package com.spark.bitrade.dao;

import com.spark.bitrade.entity.ExchangeOrderDetail;
import com.spark.bitrade.entity.ExchangeTrade;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExchangeTradeRepository extends MongoRepository<ExchangeTrade,String> {
}
