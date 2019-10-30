package com.spark.bitrade.service;

import com.spark.bitrade.dao.MemberApplicationConfigDao;
import com.spark.bitrade.entity.MemberApplicationConfig;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberApplicationConfigService extends TopBaseService<MemberApplicationConfig,MemberApplicationConfigDao>{

    @Autowired
    public void setDao(MemberApplicationConfigDao dao) {
        super.setDao(dao);
    }

    public MemberApplicationConfig get(){
        List<MemberApplicationConfig> list = dao.findAll() ;
        return list!=null&&list.size()>0 ? list.get(0) : null;
    }
}
