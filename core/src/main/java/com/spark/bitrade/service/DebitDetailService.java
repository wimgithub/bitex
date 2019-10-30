package com.spark.bitrade.service;

import com.spark.bitrade.dao.DebitDetailDao;
import com.spark.bitrade.entity.DebitDetail;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebitDetailService extends BaseService {
    @Autowired
    private DebitDetailDao debitDetailDao;

    public DebitDetail save(DebitDetail debitDetail) {
        return debitDetailDao.save(debitDetail);
    }

    public DebitDetail findOne(Long id) {
        return debitDetailDao.findOne(id);
    }

    public Page<DebitDetail> findPageByMemberAndStatus(Member member, int status, int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<DebitDetail> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("member", member, false));
        criteria.add(Restrictions.eq("status", status, false));
        return debitDetailDao.findAll(criteria, pageRequest);
    }

    public List<DebitDetail> findAllByStatus(int status) {
        Criteria<DebitDetail> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("status", status, false));
        return debitDetailDao.findAll(criteria);
    }
}
