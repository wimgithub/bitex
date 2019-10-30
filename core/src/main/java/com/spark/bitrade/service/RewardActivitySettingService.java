package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.RewardActivitySettingDao;
import com.spark.bitrade.dto.PageParam;
import com.spark.bitrade.entity.QRewardActivitySetting;
import com.spark.bitrade.entity.QRewardPromotionSetting;
import com.spark.bitrade.entity.RewardActivitySetting;
import com.spark.bitrade.entity.RewardPromotionSetting;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardActivitySettingService extends TopBaseService<RewardActivitySetting,RewardActivitySettingDao> {

    @Autowired
    public void setDao(RewardActivitySettingDao dao) {
        this.dao = dao ;
    }


    public RewardActivitySetting findByType(ActivityRewardType type){
        return dao.findByStatusAndType(BooleanEnum.IS_TRUE, type);
    }

    public RewardActivitySetting save(RewardActivitySetting rewardActivitySetting){
        return dao.save(rewardActivitySetting);
    }

   /* public List<RewardActivitySetting> page(Predicate predicate){
        Pageable pageable = new PageRequest()
        Iterable<RewardActivitySetting> iterable = rewardActivitySettingDao.findAll(predicate, QRewardActivitySetting.rewardActivitySetting.updateTime.desc());
        return (List<RewardActivitySetting>) iterable ;
    }*/


}
