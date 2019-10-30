package com.spark.bitrade.service;

import com.spark.bitrade.dao.DebitRecordDao;
import com.spark.bitrade.entity.DebitDetail;
import com.spark.bitrade.entity.DebitRecord;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DebitRecordService extends BaseService {
    @Autowired
    private DebitRecordDao debitRecordDao;

    public DebitRecord save(DebitRecord debitRecord) {
        return debitRecordDao.save(debitRecord);
    }

    public DebitRecord findOne(Long id) {
        return debitRecordDao.findOne(id);
    }

    public Page<DebitRecord> findPageByDebit(DebitDetail debitDetail, int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);

        Criteria<DebitRecord> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("debitDetail", debitDetail, false));
        return debitRecordDao.findAll(criteria, pageRequest);
    }
}
