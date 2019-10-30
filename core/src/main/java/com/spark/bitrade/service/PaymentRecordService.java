package com.spark.bitrade.service;

import com.spark.bitrade.dao.PaymentRecordDao;
import com.spark.bitrade.entity.PaymentRecord;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class PaymentRecordService extends BaseService {
    @Autowired
    private PaymentRecordDao paymentRecordDao;

    public PaymentRecord save(PaymentRecord paymentRecord) {
        return paymentRecordDao.save(paymentRecord);
    }

    public Page<PaymentRecord> getListByMember(Long memberId,int pageNo, int pageSize, Long relativeMemberId, String symbol) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<PaymentRecord> criteria = new Criteria<>();

        if (relativeMemberId != null) {
            criteria.add(Restrictions.or(
                    Restrictions.eq("fromMemberId",memberId,false),
                    Restrictions.eq("toMemberId",memberId,false))
            );
            criteria.add(Restrictions.or(
                    Restrictions.eq("fromMemberId",relativeMemberId,false),
                    Restrictions.eq("toMemberId",relativeMemberId,false))
            );
        }else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("fromMemberId",memberId,false),
                    Restrictions.eq("toMemberId",memberId,false))
            );
        }

        if (!StringUtils.isEmpty(symbol)) criteria.add(Restrictions.eq("symbol", symbol, false));
        return paymentRecordDao.findAll(criteria, pageRequest);
    }
}