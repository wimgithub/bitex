package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.vo.ImportXmlVO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface MemberWalletDao extends BaseDao<MemberWallet> {

    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Transactional
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount where wallet.id = :walletId")
    int increaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 减少钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int decreaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 解冻钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Transactional
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount,wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int thawBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 冻结钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Transactional
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount,wallet.frozenBalance=wallet.frozenBalance + :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int freezeBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 减少冻结余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int decreaseFrozen(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    MemberWallet findByCoinAndAddress(Coin coin, String address);

    MemberWallet findByCoinAndMemberId(Coin coin, Long memberId);

    List<MemberWallet> findAllByMemberId(Long memberId);

    List<MemberWallet> findAllByCoin(Coin coin);

    @Query(value="select ifnull(sum(a.balance),0)+ ifnull(sum(a.frozen_balance),0) as allBalance from member_wallet a where a.coin_id = :coinName",nativeQuery = true)
    BigDecimal getWalletAllBalance(@Param("coinName")String coinName);


    @Query("select m from MemberWallet m where m.coin=:coin and m.memberId in (:memberIdList)")
    List<MemberWallet> findALLByCoinIdAndMemberIdList(@Param("coin")Coin coin,@Param("memberIdList")List<Long> memberIdList);

    @Query("select m from MemberWallet m where m.frozenBalance > 0")
    List<MemberWallet> findByFrozenBalanceBiggerThenZero();
}
