package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.FinanceProduct;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface FinanceProductDao extends BaseDao<FinanceProduct> {

    List<FinanceProduct> findAllByStatus(Integer status);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update FinanceProduct set status=2,endDate=:endDate where status=1 and id=:id")
    int putOff(@Param("id") Long id,@Param("endDate") Date endDate);
}
