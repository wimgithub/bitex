package com.spark.bitrade.service;

import com.spark.bitrade.dao.FinanceProductDetailDao;
import com.spark.bitrade.entity.FinanceProduct;
import com.spark.bitrade.entity.FinanceProductDetail;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class FinanceProductDetailService extends BaseService {

    @Autowired
    private FinanceProductDetailDao financeProductDetailDao;
    @Autowired
    private FinanceProductService financeProductService;

    public FinanceProductDetail save(FinanceProductDetail financeProductDetail) {
        return financeProductDetailDao.saveAndFlush(financeProductDetail);
    }

    public FinanceProductDetail findOne(Long financeProductDetailId) {
        return financeProductDetailDao.findOne(financeProductDetailId);
    }

    public Page<FinanceProductDetail> getListByProduct(FinanceProduct financeProduct, int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<FinanceProductDetail> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("financeProduct", financeProduct,false));

        return financeProductDetailDao.findAll(criteria, pageRequest);
    }

    public List<FinanceProductDetail> getListByProduct(FinanceProduct financeProduct) {
        return financeProductDetailDao.findAllByFinanceProductAndStatus(financeProduct,0);
    }

    public Page<FinanceProductDetail> getListByMember(Member member, int pageNo, int pageSize, Long productId, Integer productType, Integer status) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<FinanceProductDetail> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("member", member,false));

        if (productId != null) {
            FinanceProduct financeProduct = financeProductService.findOne(productId);
            criteria.add(Restrictions.eq("financeProduct", financeProduct,false));
        }
        if (productType != null) {
            criteria.add(Restrictions.eq("financeProduct.type", productType,false));
        }
        if (status != null) {
            criteria.add(Restrictions.eq("status", status, false));
        }

        return financeProductDetailDao.findAll(criteria, pageRequest);
    }

    public BigDecimal getBuyAmount(FinanceProduct financeProduct) {
        return financeProductDetailDao.getTotalAmount(financeProduct.getId());
    }

    public void putOffById(FinanceProductDetail financeProductDetail) {
        financeProductDetailDao.putOffById(financeProductDetail.getId(), new Date());
    }

    public void breakUp(FinanceProductDetail financeProductDetail) {
        financeProductDetailDao.breakUp(financeProductDetail.getId(), new Date());
    }
}
