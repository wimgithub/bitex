package com.spark.bitrade.service;

import com.spark.bitrade.dao.RewardWalletDao;
import com.spark.bitrade.entity.RewardWallet;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分红钱包
 */
@Service
public class RewardWalletService extends BaseService {
    @Autowired
    private RewardWalletDao rewardWalletDao;

    /**
     * 查询用户钱包信息
     * @param memberId
     * @return
     */
    public RewardWallet findRewardWalletByMemberId(Long memberId ){
        return rewardWalletDao.findRewardWalletByMemberId(memberId);
    }

    /**
     * 保存奖励钱包
     * @param wallet
     * @return
     */
    public RewardWallet save(RewardWallet wallet){
        return rewardWalletDao.save(wallet);
    }

    /**
     * 查询用户钱包信息
     * @param memberId
     * @return
     */
    public RewardWallet findRewardWalletByMemberIdAndCoinUnit(Long memberId ,String unit){
        return rewardWalletDao.findRewardWalletByMemberIdAndCoinUnit(memberId,unit);
    }

    /**
     * 根据用户ID查询用户的分红奖励记录
     * @param memberId
     * @return
     */
    public List<RewardWallet> findAllByMemberId(Long memberId){
            return rewardWalletDao.findAllByMemberId(memberId);
    }

    /**
     * 查询用户的分红奖励记录
     * @param
     * @return
     */
    public List<RewardWallet> findAll(){
        return rewardWalletDao.findAll();
    }
}
