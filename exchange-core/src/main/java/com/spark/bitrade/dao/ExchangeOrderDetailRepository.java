package com.spark.bitrade.dao;

import com.spark.bitrade.entity.ExchangeOrderDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExchangeOrderDetailRepository extends MongoRepository<ExchangeOrderDetail,String>{
    List<ExchangeOrderDetail> findAllByOrderId(String orderId);
}
