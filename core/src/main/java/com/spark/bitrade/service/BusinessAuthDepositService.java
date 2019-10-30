package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.BusinessAuthDepositDao;
import com.spark.bitrade.entity.BusinessAuthApply;
import com.spark.bitrade.entity.BusinessAuthDeposit;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Service
public class BusinessAuthDepositService extends BaseService {
    @Autowired
    private BusinessAuthDepositDao businessAuthDepositDao;

    public Page<BusinessAuthDeposit> findAll(Predicate predicate, PageModel pageModel) {
        return businessAuthDepositDao.findAll(predicate, pageModel.getPageable());
    }

    public List<BusinessAuthDeposit> findAllByStatus(CommonStatus status){
        return businessAuthDepositDao.findAllByStatus(status);
    }

    public BusinessAuthDeposit findById(Long id){
        return businessAuthDepositDao.findOne(id);
    }

    public void save(BusinessAuthDeposit businessAuthDeposit){
        businessAuthDepositDao.save(businessAuthDeposit);
    }

    public void update(BusinessAuthDeposit businessAuthDeposit){
        businessAuthDepositDao.save(businessAuthDeposit);
    }
}
