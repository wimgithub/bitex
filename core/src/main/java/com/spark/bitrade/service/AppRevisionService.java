package com.spark.bitrade.service;

import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.dao.AppRevisionDao;
import com.spark.bitrade.entity.AppRevision;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:19
 */
@Service
public class AppRevisionService extends TopBaseService<AppRevision, AppRevisionDao> {

    @Autowired
    public void setDao(AppRevisionDao dao) {
        super.setDao(dao);
    }

    public AppRevision findRecentVersion(Platform p){
        return dao.findAppRevisionByPlatformOrderByIdDesc(p);
    }
}
