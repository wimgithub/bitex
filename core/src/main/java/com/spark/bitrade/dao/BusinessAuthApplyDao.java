package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BusinessAuthApply;
import com.spark.bitrade.entity.Member;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
public interface BusinessAuthApplyDao extends BaseDao<BusinessAuthApply> {

    List<BusinessAuthApply> findByMemberOrderByIdDesc(Member member);

    List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatusOrderByIdDesc(Member member, CertifiedBusinessStatus certifiedBusinessStatus);

    long countAllByCertifiedBusinessStatus(CertifiedBusinessStatus status);

}
