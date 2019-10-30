package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.HotTransferRecord;

public interface HotTransferRecordDao extends BaseDao<HotTransferRecord> {
        HotTransferRecord findById(Long id);
}
