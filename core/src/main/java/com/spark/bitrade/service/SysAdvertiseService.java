package com.spark.bitrade.service;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.dao.SysAdvertiseDao;
import com.spark.bitrade.entity.SysAdvertise;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.spark.bitrade.entity.QSysAdvertise.sysAdvertise;

/**
 * @author rongyu
 * @description 系统广告service
 * @date 2018/1/6 16:45
 */
@Service
public class SysAdvertiseService extends BaseService {
    @Autowired
    SysAdvertiseDao sysAdvertiseDao;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<SysAdvertise> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<SysAdvertise> list;
        JPAQuery<SysAdvertise> jpaQuery = queryFactory.selectFrom(sysAdvertise);
        if (predicateList != null)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, sysAdvertise.createTime))
                    .offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, sysAdvertise.createTime)).fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    public SysAdvertise findOne(String serialNumber) {
        return sysAdvertiseDao.findOne(serialNumber);
    }

    public int getMaxSort(){
        return sysAdvertiseDao.findMaxSort();
    }

    public SysAdvertise save(SysAdvertise sysAdvertise) {
        return sysAdvertiseDao.save(sysAdvertise);
    }

    public List<SysAdvertise> findAll() {
        return sysAdvertiseDao.findAll();
    }

    public void deleteOne(String serialNumber) {
        sysAdvertiseDao.delete(serialNumber);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String[] array) {
        for (String serialNumber : array) {
            sysAdvertiseDao.delete(serialNumber);
        }
    }

    public List<SysAdvertise> findAllNormal(SysAdvertiseLocation sysAdvertiseLocation,String sysLanguage) {
        return sysAdvertiseDao.findAllByStatusAndSysAdvertiseLocationAndSysLanguageOrderBySortDesc(CommonStatus.NORMAL, sysAdvertiseLocation,sysLanguage);
    }


    public List<SysAdvertise> findAll(List<Predicate> predicateList) {
        List<SysAdvertise> list;
        JPAQuery<SysAdvertise> jpaQuery = queryFactory.selectFrom(sysAdvertise);
        if (predicateList != null)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, sysAdvertise.createTime)).fetch();
        return list;
    }

    public Page<SysAdvertise> findAll(Predicate predicate, Pageable pageable) {
        return sysAdvertiseDao.findAll(predicate, pageable);
    }

}
