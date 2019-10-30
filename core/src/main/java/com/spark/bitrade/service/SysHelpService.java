package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysHelpClassification;
import com.spark.bitrade.dao.SysHelpDao;
import com.spark.bitrade.entity.QSysHelp;
import com.spark.bitrade.entity.SysHelp;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author rongyu
 * @description
 * @date 2018/1/9 10:00
 */
@Service
public class SysHelpService extends BaseService {
    @Autowired
    private SysHelpDao sysHelpDao;

    public SysHelp save(SysHelp sysHelp) {
        return sysHelpDao.save(sysHelp);
    }

    public List<SysHelp> findAll(Predicate predicate,Sort sort) {
        return (List<SysHelp>) sysHelpDao.findAll(predicate,sort);
    }

    public SysHelp findOne(Long id) {
        return sysHelpDao.findOne(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            sysHelpDao.delete(id);
        }
    }

    public int getMaxSort(){
        return sysHelpDao.findMaxSort();
    }

    public List<SysHelp> findBySysHelpClassification(SysHelpClassification sysHelpClassification) {
        return sysHelpDao.findAllBySysHelpClassificationAndStatusNot(sysHelpClassification,CommonStatus.ILLEGAL);
    }

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<SysHelp> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<SysHelp> jpaQuery = queryFactory.selectFrom(QSysHelp.sysHelp);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(QSysHelp.sysHelp.createTime.desc());
        List<SysHelp> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        PageResult<SysHelp> page = new PageResult<>(list, pageNo, pageSize, count);
        return page;
    }

    public Page<SysHelp> findAll(Predicate predicate, Pageable pageable) {
        return sysHelpDao.findAll(predicate, pageable);
    }
}
