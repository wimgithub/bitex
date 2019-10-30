package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.DividendStartRecordDao;
import com.spark.bitrade.entity.DividendStartRecord;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月22日
 */
@Service
public class DividendStartRecordService extends TopBaseService<DividendStartRecord, DividendStartRecordDao> {

    @Autowired
    public void setDao(DividendStartRecordDao dao) {
        super.setDao(dao);
    }

    public List<DividendStartRecord> matchRecord(long start, long end, String unit) {
        return dao.findAllByTimeAndUnit(start, end, unit);
    }

    public DividendStartRecord save(DividendStartRecord dividendStartRecord) {
        return dao.save(dividendStartRecord);
    }


}
