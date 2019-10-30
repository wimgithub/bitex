package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.dao.DebitConfigDao;
import com.spark.bitrade.entity.DebitConfig;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebitConfigService extends BaseService {
    @Autowired
    private DebitConfigDao debitConfigDao;

    public DebitConfig findOne(Long id) {
        return debitConfigDao.findOne(id);
    }

    public DebitConfig save(DebitConfig debitConfig) {
        return debitConfigDao.save(debitConfig);
    }

    public List<DebitConfig> getAll() {
        return debitConfigDao.findAll();
    }

    public DebitConfig getConfigByType(Integer type) {
        DebitConfig debitConfig = debitConfigDao.findDebitConfigByType(type);
        return debitConfig;
    }

    public JSONObject getConfigJsonByType(Integer type) {
        DebitConfig debitConfig = debitConfigDao.findDebitConfigByType(type);
        if (debitConfig == null) return null;
        return JSON.parseObject(debitConfig.getContent());
    }
}