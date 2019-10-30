package com.spark.bitrade.dao;

import com.spark.bitrade.constant.DepositStatusEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DepositRecord;
import com.spark.bitrade.entity.Member;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
public interface DepositRecordDao extends BaseDao<DepositRecord> {
    public DepositRecord findById(String id);

    public List<DepositRecord> findByMemberAndStatus(Member member, DepositStatusEnum status);
}
