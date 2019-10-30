package com.spark.bitrade.service;

import com.spark.bitrade.dao.MemberSignRecordDao;
import com.spark.bitrade.entity.MemberSignRecord;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author rongyu
 * @Description:
 * @date 2018/5/410:19
 */
@Service
public class MemberSignRecordService extends TopBaseService<MemberSignRecord, MemberSignRecordDao> {
    @Autowired
    public void setDao(MemberSignRecordDao dao) {
        super.setDao(dao);
    }
}
