package com.spark.bitrade.config;

import com.spark.bitrade.Trader.CoinTrader;
import com.spark.bitrade.Trader.CoinTraderFactory;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.ExchangeOrderService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
@Configuration
public class CoinTraderConfig {

    /**
     * 配置交易处理类
     * @param exchangeCoinService
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public CoinTraderFactory getCoinTrader(ExchangeCoinService exchangeCoinService, KafkaTemplate<String,String> kafkaTemplate, ExchangeOrderService exchangeOrderService){
        CoinTraderFactory factory = new CoinTraderFactory();
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        for(ExchangeCoin coin:coins) {
            log.info("init trader,symbol={}",coin.getSymbol());
            CoinTrader trader = new CoinTrader(coin.getSymbol());
            trader.setKafkaTemplate(kafkaTemplate);
            trader.setBaseCoinScale(coin.getBaseCoinScale());
            trader.setCoinScale(coin.getCoinScale());
            trader.stopTrading();
            trader.initialize();
            factory.addTrader(coin.getSymbol(),trader);
        }
        return factory;
    }

}
