package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DataDictionary;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:15
 */
public interface DataDictionaryDao extends BaseDao<DataDictionary> {
    DataDictionary findByBond(String bond);
}
