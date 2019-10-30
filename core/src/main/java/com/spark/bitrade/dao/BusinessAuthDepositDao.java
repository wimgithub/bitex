package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BusinessAuthDeposit;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/5
 */
public interface BusinessAuthDepositDao extends BaseDao<BusinessAuthDeposit> {
    public List<BusinessAuthDeposit> findAllByStatus(CommonStatus status);
}
