package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.dao.RewardRecordDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.RewardRecord;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardRecordService extends BaseService {
    @Autowired
    private RewardRecordDao rewardRecordDao;

    public RewardRecord save(RewardRecord rewardRecord){
        return rewardRecordDao.save(rewardRecord);
    }

    public List<RewardRecord> queryRewardPromotionList(Member member){
        return rewardRecordDao.findAllByMemberAndType(member, RewardRecordType.PROMOTION);
    }

    public Map<String,BigDecimal> getAllPromotionReward(long memberId,RewardRecordType type){
        List<Object[]> list = rewardRecordDao.getAllPromotionReward(memberId,type.getOrdinal());
        Map<String,BigDecimal> map = new HashMap<>() ;
        for(Object[] array:list){
            map.put(array[0].toString(),(BigDecimal)array[1]);
        }
        return map ;
    }


    /**
     * @param predicate 筛选条件
     * @param pageModel 分页对象
     * @return
     */
    @Transactional(readOnly = true)
    public Page<RewardRecord> findAll(Predicate predicate, PageModel pageModel) {
        return rewardRecordDao.findAll(predicate, pageModel.getPageable());
    }

    @Transactional(readOnly = true)
    public Long  findCount(Predicate predicate) {
        return rewardRecordDao.count(predicate);
    }

}
