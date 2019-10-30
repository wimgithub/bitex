package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.FinanceProduct;
import com.spark.bitrade.entity.FinanceProductDetail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface FinanceProductDetailDao extends BaseDao<FinanceProductDetail> {

    @Query(value = "select ifnull(sum(amount),0) from finance_product_detail a where a.finance_product_id=:financeProductId and status=0",nativeQuery = true)
    BigDecimal getTotalAmount(@Param("financeProductId") Long financeProductId);

    List<FinanceProductDetail> findAllByFinanceProductAndStatus(FinanceProduct financeProduct, Integer status);

    //产品下架
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update FinanceProductDetail set status=2,endTime=:endDate where financeProduct=:financeProduct")
    int putOff(@Param("financeProduct") FinanceProduct financeProduct,@Param("endDate") Date endDate);

    //某个明细下架
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update FinanceProductDetail set status=2,endTime=:endDate where id=:id")
    int putOffById(@Param("id") Long id,@Param("endDate") Date endDate);

    //某个明细解约
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update FinanceProductDetail set status=1,endTime=:endDate where id=:id")
    int breakUp(@Param("id") Long id,@Param("endDate") Date endDate);
}