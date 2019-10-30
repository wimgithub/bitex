package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.FinanceProductDao;
import com.spark.bitrade.dao.FinanceProductDetailDao;
import com.spark.bitrade.entity.FinanceProduct;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FinanceProductService extends BaseService {

    @Autowired
    private FinanceProductDao financeProductDao;
    @Autowired
    private FinanceProductDetailDao financeProductDetailDao;

    public FinanceProduct save(FinanceProduct financeProduct) {
        return financeProductDao.saveAndFlush(financeProduct);
    }

    public FinanceProduct findOne(Long financeProductId) {
        return financeProductDao.findOne(financeProductId);
    }

    public List<FinanceProduct> getAllList() {
        return financeProductDao.findAllByStatus(1);
    }

    public Page<FinanceProduct> getAll(Predicate predicate, Pageable pageable) {
        return financeProductDao.findAll(predicate, pageable);
    }

    public Page<FinanceProduct> getAll(int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo,pageSize,sort);
        return financeProductDao.findAll(pageRequest);
    }

    public Page<FinanceProduct> getAllAvailable(int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo,pageSize,sort);

        Criteria<FinanceProduct> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("status", 1,false));
        return financeProductDao.findAll(criteria, pageRequest);
    }

    public void putOff(FinanceProduct financeProduct) {
        if (financeProductDao.putOff(financeProduct.getId(), new Date()) == 1) {
            financeProductDetailDao.putOff(financeProduct,new Date());
        }
    }
}
