package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.RewardPromotionSetting;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardPromotionSettingDao extends BaseDao<RewardPromotionSetting> {
    RewardPromotionSetting findByStatusAndType(BooleanEnum booleanEnum, PromotionRewardType type);
}
