package com.spark.bitrade.dao;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysHelpClassification;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.SysHelp;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author rongyu
 * @description
 * @date 2018/1/9 9:58
 */
public interface SysHelpDao extends BaseDao<SysHelp> {
    List<SysHelp> findAllBySysHelpClassificationAndStatusNot(SysHelpClassification sysHelpClassification,CommonStatus commonStatus);

    @Query("select max(s.sort) from SysHelp s")
    int findMaxSort();
}
