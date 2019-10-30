package com.spark.bitrade.dao;

import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.AppRevision;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:18
 */
public interface AppRevisionDao extends BaseDao<AppRevision> {
    AppRevision findAppRevisionByPlatformOrderByIdDesc(Platform platform);
}
