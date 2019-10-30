package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.dao.RewardPromotionSettingDao;
import com.spark.bitrade.entity.QRewardPromotionSetting;
import com.spark.bitrade.entity.RewardPromotionSetting;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardPromotionSettingService  extends TopBaseService<RewardPromotionSetting,RewardPromotionSettingDao> {

    @Autowired
    public void setDao(RewardPromotionSettingDao dao) {
        super.setDao(dao);
    }

    public RewardPromotionSetting findByType(PromotionRewardType type){
        return dao.findByStatusAndType(BooleanEnum.IS_TRUE, type);
    }

    public RewardPromotionSetting save(RewardPromotionSetting setting){
        return dao.save(setting);
    }

    public void deletes(long[] ids){
        for(long id : ids){
            delete(id);
        }
    }

}
