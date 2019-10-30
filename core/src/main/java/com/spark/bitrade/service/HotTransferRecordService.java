package com.spark.bitrade.service;

import com.spark.bitrade.dao.HotTransferRecordDao;
import com.spark.bitrade.entity.HotTransferRecord;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotTransferRecordService extends TopBaseService<HotTransferRecord,HotTransferRecordDao> {
    @Autowired
    private HotTransferRecordDao hotTransferRecordDao;

    @Autowired
    public void setDao(HotTransferRecordDao dao) {
        super.setDao(dao);
    }

    public HotTransferRecord findById(Long id){
        return hotTransferRecordDao.findById(id);
    }
}
