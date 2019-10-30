package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.entity.SysAdvertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * @author rongyu
 * @description
 * @date 2018/1/6 16:44
 */
public interface SysAdvertiseDao extends JpaRepository<SysAdvertise, String>, JpaSpecificationExecutor<SysAdvertise>, QueryDslPredicateExecutor<SysAdvertise> {
    List<SysAdvertise> findAllByStatusAndSysAdvertiseLocationAndSysLanguageOrderBySortDesc(CommonStatus status, SysAdvertiseLocation sysAdvertiseLocation,String sysLanguage);

    @Query("select max(s.sort) from SysAdvertise s")
    int findMaxSort();

}
