package com.spark.bitrade.dao;

import com.spark.bitrade.entity.PlatformTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformTransactionDao extends JpaRepository<PlatformTransaction,Long>{
}
