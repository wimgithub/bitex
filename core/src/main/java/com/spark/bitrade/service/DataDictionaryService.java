package com.spark.bitrade.service;

import com.spark.bitrade.dao.DataDictionaryDao;
import com.spark.bitrade.entity.DataDictionary;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:19
 */
@Service
public class DataDictionaryService extends TopBaseService<DataDictionary, DataDictionaryDao> {
    @Autowired
    DataDictionaryDao dataDictionaryDao;

    @Autowired
    public void setDao(DataDictionaryDao dao) {
        super.setDao(dao);
    }

    public DataDictionary findByBond(String bond) {
        return dataDictionaryDao.findByBond(bond);
    }

}
