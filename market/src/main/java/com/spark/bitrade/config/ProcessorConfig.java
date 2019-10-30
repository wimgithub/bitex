package com.spark.bitrade.config;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.handler.MongoMarketHandler;
import com.spark.bitrade.handler.NettyHandler;
import com.spark.bitrade.handler.WebsocketMarketHandler;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.processor.DefaultCoinProcessor;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class ProcessorConfig {

    @Bean
    public CoinProcessorFactory processorFactory(MongoMarketHandler mongoMarketHandler,
                                                 WebsocketMarketHandler wsHandler,
                                                 NettyHandler nettyHandler,
                                                 MarketService marketService,
                                                 CoinExchangeRate exchangeRate,
                                                 ExchangeCoinService coinService) {

        log.info("====initialized CoinProcessorFactory start==================================");

        CoinProcessorFactory factory = new CoinProcessorFactory();
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        log.info("exchange-coin result:{}",coins);
        String symbol= SysConstant.usdt_husd;
        String[] symbols=symbol.split("/");
        createProcessor(symbols[1],symbol,4,4,mongoMarketHandler, wsHandler,nettyHandler,marketService,exchangeRate,factory);
        for (ExchangeCoin coin : coins) {
            createProcessor(coin.getBaseSymbol(),coin.getSymbol(),coin.getCoinScale(),coin.getBaseCoinScale(),mongoMarketHandler,
                    wsHandler,nettyHandler,marketService,exchangeRate,factory);
            /*CoinProcessor processor = new DefaultCoinProcessor(coin.getSymbol(), coin.getBaseSymbol());
            processor.addHandler(mongoMarketHandler);
            processor.addHandler(wsHandler);
            processor.addHandler(nettyHandler);
            processor.setMarketService(marketService);
            processor.setExchangeRate(exchangeRate);
            processor.setScale(coin.getCoinScale(),coin.getBaseCoinScale());
            factory.addProcessor(coin.getSymbol(), processor);*/
        }
        log.info("====initialized CoinProcessorFactory completed====");
        log.info("CoinProcessorFactory = ", factory);
        exchangeRate.setCoinProcessorFactory(factory);
        return factory;
    }

    private void createProcessor(String baseSymbol,String symbol,int coinScale,int baseCoinScale,
                                 MongoMarketHandler mongoMarketHandler,
                                 WebsocketMarketHandler wsHandler,
                                 NettyHandler nettyHandler,
                                 MarketService marketService,
                                 CoinExchangeRate exchangeRate,
                                 CoinProcessorFactory factory){
        CoinProcessor processor = new DefaultCoinProcessor(symbol, baseSymbol);
        processor.addHandler(mongoMarketHandler);
        processor.addHandler(wsHandler);
        processor.addHandler(nettyHandler);
        processor.setMarketService(marketService);
        processor.setExchangeRate(exchangeRate);
        processor.setScale(coinScale,baseCoinScale);
        factory.addProcessor(symbol, processor);
    }

}
