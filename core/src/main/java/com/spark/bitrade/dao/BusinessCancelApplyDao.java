package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BusinessCancelApply;
import com.spark.bitrade.entity.Member;

import java.util.List;

/**
 * @author jiangtao
 * @date 2018/5/17
 */
public interface BusinessCancelApplyDao extends BaseDao<BusinessCancelApply>{

    List<BusinessCancelApply> findByMemberAndStatusOrderByIdDesc(Member member , CertifiedBusinessStatus status);

    List<BusinessCancelApply> findByMemberOrderByIdDesc(Member member);

    long countAllByStatus(CertifiedBusinessStatus status);
}
