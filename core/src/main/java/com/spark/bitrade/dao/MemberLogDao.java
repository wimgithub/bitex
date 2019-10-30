package com.spark.bitrade.dao;

import com.spark.bitrade.entity.MemberLog;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MemberLogDao extends MongoRepository<MemberLog,Long> {
}
