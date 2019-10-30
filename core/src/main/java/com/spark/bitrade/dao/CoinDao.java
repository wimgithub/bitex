package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dto.CoinDTO;
import com.spark.bitrade.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author rongyu
 * @description 货币操作
 * @date 2017/12/29 14:41
 */
@Repository
public interface CoinDao extends JpaRepository<Coin, String>, JpaSpecificationExecutor<Coin>, QueryDslPredicateExecutor<Coin> {
    Coin findByUnit(String unit);

    Coin findByTokenAddress(String tokenAddress);

    List<Coin> findAllByCanWithdrawAndStatusAndHasLegal(BooleanEnum is, CommonStatus status, boolean hasLegal);

    Coin findCoinByIsPlatformCoin(BooleanEnum is);

    List<Coin> findByHasLegal(Boolean hasLegal);

    @Query("select a from Coin a where a.unit in (:units) ")
    List<Coin> findAllByOtc(@Param("units") List<String> otcUnits);

    @Query("select a.name from Coin a")
    List<String> findAllName();

    @Query(value = "select  new com.spark.bitrade.dto.CoinDTO(a.name,a.unit) from Coin a")
    List<CoinDTO> findAllNameAndUnit();

    @Query("select a.name from Coin a where a.hasLegal = true ")
    List<String> findAllCoinNameLegal();

    @Query("select a.unit from Coin a where a.enableRpc = 1")
    List<String> findAllRpcUnit();

    List<Coin> findAllByIsPlatformCoin(BooleanEnum isPlatformCoin);

    /**
     * 查询指定币种的总额
     * @param coin
     * @return
     */
    @Query("SELECT sum(a.balance) FROM MemberWallet a WHERE a.coin = :coin")
    BigDecimal sumBalance(@Param("coin") Coin coin);

    /**
     * 根据用户ID查询指定币种的总额
     * @param coin
     * @return
     */
    @Query("SELECT a.balance FROM MemberWallet a WHERE a.coin = :coin AND a.memberId = :memberId")
    BigDecimal getBalanceByMemberIdAndCoinId(@Param("coin") Coin coin ,@Param("memberId") Long memberId);

    @Query("select a from Coin a order by a.sort")
    List<Coin> findAllOrderBySort();

    List<Coin> findAllByStatus(CommonStatus status);

    List<Coin> findByStatus(CommonStatus status);

}
