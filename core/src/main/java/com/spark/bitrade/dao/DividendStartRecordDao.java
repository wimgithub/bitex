package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DividendStartRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月22日
 */
public interface DividendStartRecordDao extends BaseDao<DividendStartRecord> {

    @Query("select a from DividendStartRecord a where ((a.start<:start and a.end>=:start) or (a.start<:en and a.end>=:en) " +
            " or (a.start>=:start and a.end<:en ) or (a.start<=:start and a.end>:en)) and a.unit=:unit")
    List<DividendStartRecord> findAllByTimeAndUnit(@Param("start") long start, @Param("en") long end, @Param("unit") String unit);
}
