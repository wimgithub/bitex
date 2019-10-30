package com.spark.bitrade.dao;

import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.RewardActivitySetting;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardActivitySettingDao extends BaseDao<RewardActivitySetting> {
    RewardActivitySetting findByStatusAndType(BooleanEnum booleanEnum, ActivityRewardType type);
}
