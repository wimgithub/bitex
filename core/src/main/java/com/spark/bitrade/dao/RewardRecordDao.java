package com.spark.bitrade.dao;

import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.RewardRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardRecordDao extends BaseDao<RewardRecord> {
    List<RewardRecord> findAllByMemberAndType(Member member, RewardRecordType type);

    @Query(value = "select coin_id , sum(amount) from reward_record where member_id = :memberId and type = :type group by coin_id",nativeQuery = true)
    List<Object[]> getAllPromotionReward(@Param("memberId") long memberId ,@Param("type") int type);
}
