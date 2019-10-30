package com.spark.bitrade.config;

import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.system.CoinExchangeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class CoinExchangeFactoryConfig {
    @Autowired
    private CoinService coinService;

    @Bean
    public CoinExchangeFactory createCoinExchangeFactory() {
        List<Coin> coinList = coinService.findAll();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coinList.forEach(coin ->
                factory.set(coin.getUnit(), coin.getUsdRate(), coin.getCnyRate(),coin.getSgdRate())
        );
        return factory;
    }
}
