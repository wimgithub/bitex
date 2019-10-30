package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ExchangeOrderRepository extends JpaRepository<ExchangeOrder, String>, JpaSpecificationExecutor<ExchangeOrder>, QueryDslPredicateExecutor<ExchangeOrder> {
    ExchangeOrder findByOrderId(String orderId);

    @Modifying
    @Query("update ExchangeOrder exchange set exchange.tradedAmount = exchange.tradedAmount + ?1  where exchange.orderId = ?2")
    int increaseTradeAmount(BigDecimal amount, String orderId);

    @Modifying
    @Query("update ExchangeOrder  exchange set exchange.status = :status where exchange.orderId = :orderId")
    int updateStatus(@Param("orderId") String orderId, @Param("status") ExchangeOrderStatus status);

    @Query(value="select coin_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit",nativeQuery = true)
    List<Object[]> getExchangeTurnoverCoin(@Param("date") String date);

    @Query(value="select base_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(turnover) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit",nativeQuery = true)
    List<Object[]> getExchangeTurnoverBase(@Param("date") String date);

    @Query(value="select base_symbol , coin_symbol,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount),sum(turnover) from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by base_symbol,coin_symbol",nativeQuery = true)
    List<Object[]> getExchangeTurnoverSymbol(@Param("date") String date) ;

    /**
     * 查询该用户近30天的交易量
     * @param memberId
     * @return
     */
    @Query("SELECT count(a.orderId) FROM ExchangeOrder a WHERE  a.memberId = :memberId AND a.completedTime BETWEEN :beginDate AND :endDate")
    int countExchangeOrderByMemberId(@Param("memberId") Long memberId,@Param("beginDate") Long beginDate,@Param("endDate") Long endDate);
    List<ExchangeOrder> findAllByMemberIdAndMarginTradeAndStatus(Long memberId, BooleanEnum marginTrade, ExchangeOrderStatus status);

    @Query(value = "select o.memberId,count(o.memberId) as c from ExchangeOrder o where o.time between :startTime and :endTime GROUP BY o.memberId")
    List<Object[]> countOrdersByMemberIdAndCreateTime(@Param("startTime")Long startTime, @Param("endTime")Long endTime);

    @Query("select o from ExchangeOrder o where o.status in (1,2) and o.memberId = :memberId and o.baseSymbol=:baseCoinUnit and o.direction=:direction order by o.time asc")
    List<ExchangeOrder> findAllBuyCompletedOrderByMemberIdAndCoin(@Param("memberId")Long memberId, @Param("baseCoinUnit")String coinUnit, @Param("direction")ExchangeOrderDirection direction);

    @Query("select o from ExchangeOrder o where o.status in (1,2) and o.memberId = :memberId and o.coinSymbol=:coinUnit and o.direction=:direction order by o.time asc")
    List<ExchangeOrder> findAllSellCompletedOrderByMemberIdAndCoin(@Param("memberId")Long memberId, @Param("coinUnit")String coinUnit,@Param("direction")ExchangeOrderDirection direction);
}
