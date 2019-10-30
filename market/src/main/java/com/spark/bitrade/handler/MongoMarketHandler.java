package com.spark.bitrade.handler;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoMarketHandler implements MarketHandler {
    @Autowired
    private MongoTemplate mongoTemplate;

    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        mongoTemplate.insert(exchangeTrade, "exchange_trade_" + symbol);
    }

    public void handleKLine(String symbol,KLine kLine) {
        mongoTemplate.insert(kLine,"exchange_kline_"+symbol+"_"+kLine.getPeriod());
    }
}
