package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DebitConfig;

public interface DebitConfigDao extends BaseDao<DebitConfig> {

    DebitConfig findDebitConfigByType(Integer type);
}
