package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.service.OtcCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年01月12日
 */
public interface OtcCoinDao extends BaseDao<OtcCoin> {

    OtcCoin findOtcCoinByUnitAndStatus(String unit, CommonStatus status);

    List<OtcCoin> findAllByStatus(CommonStatus status);

    OtcCoin findOtcCoinByUnit(String unit);

    @Query("select distinct a.unit from OtcCoin a where a.status = 0")
    List<String> findAllUnits();

}
