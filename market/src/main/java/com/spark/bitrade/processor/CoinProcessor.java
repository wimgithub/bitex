package com.spark.bitrade.processor;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.handler.MarketHandler;
import com.spark.bitrade.service.MarketService;

import java.util.List;

public interface CoinProcessor {

    void setIsHalt(boolean status);

    /**
     * 处理新生成的交易信息
     * @param trades
     * @return
     */
    void process(List<ExchangeTrade> trades);

    /**
     * 添加存储器
     * @param storage
     */
    void addHandler(MarketHandler storage);

    CoinThumb getThumb();

    void setMarketService(MarketService service);

    void generateKLine(int range, int field,long time);

    KLine getKLine();

    void initializeThumb();

    void autoGenerate();

    void resetThumb();

    void setExchangeRate(CoinExchangeRate coinExchangeRate);

    void update24HVolume(long time);

    void initializeUsdRate();
    String getBaseCoin();
    void setScale(int coinScale,int baseCoinScale);
}
