package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.LockPositionRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface LockPositionRecordDao extends BaseDao<LockPositionRecord> {
    LockPositionRecord findById(Long id);

    @Query("select l from LockPositionRecord l where l.status= :status and l.unlockTime < :unlockTime")
    List<LockPositionRecord> findByStatusAndUnlockTime(@Param("status")CommonStatus status,@Param("unlockTime")Date unlockTime);

    @Modifying
    @Query("update LockPositionRecord l set l.status = :status where l.id= :id")
    void unlockById(@Param("id")Long id,@Param("status")CommonStatus status);

    @Modifying
    @Query("update LockPositionRecord l set l.status = :status where l.id in (:ids)")
    void unlockByIds(@Param("ids")List<Long> ids,@Param("status")CommonStatus status);

    List<LockPositionRecord> findByMemberIdAndCoinAndStatus(Long memberId, Coin coin,CommonStatus status);
}
