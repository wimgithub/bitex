package com.spark.bitrade.dao;

import com.spark.bitrade.constant.SignStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Sign;

/**
 * @author rongyu
 * @Description:
 * @date 2018/5/311:10
 */
public interface SignDao extends BaseDao<Sign> {
    Sign findByStatus(SignStatus status);
}
