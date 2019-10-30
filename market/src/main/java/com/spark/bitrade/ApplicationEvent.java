package com.spark.bitrade;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    ExchangeCoinService coinService;
    @Autowired
    private CoinExchangeRate coinExchangeRate;
    @Value("${exchange.anchored-coins:USDT-USD}")
    private String legalAnchoredCoins;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("====初始化CoinExchangeRate====");
        String[] pairs  = legalAnchoredCoins.split(",");
        for(String pair:pairs){
            String[] symbol = pair.split("-");
            coinExchangeRate.legalAnchoredCoins.put(symbol[0].toUpperCase(),symbol[1].toUpperCase());
        }
        if(coinExchangeRate.legalAnchoredCoins.isEmpty()) {
            coinExchangeRate.legalAnchoredCoins.put("USDT", "USD");
        }
        try {
            coinExchangeRate.syncLegalRate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        log.info("legalAnchoredCoins:{}",coinExchangeRate.legalAnchoredCoins);
        log.info("====初始化CoinProcessor====");
        CoinProcessor processor0=coinProcessorFactory.getProcessor(SysConstant.usdt_husd);
        processor0.initializeThumb();
        processor0.initializeThumb();
        processor0.setIsHalt(false);
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        coins.forEach(coin->{
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            processor.initializeThumb();
            processor.initializeUsdRate();
            processor.setIsHalt(false);
        });
    }
}
