package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.Trader.CoinTrader;
import com.spark.bitrade.Trader.CoinTraderFactory;
import com.spark.bitrade.entity.ExchangeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExchangeOrderConsumer {
    @Autowired
    private CoinTraderFactory traderFactory;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @KafkaListener(topics = "exchange-order",group = "group-handle")
    public void onOrderSubmitted(ConsumerRecord<String,String> record){
        log.info("onOrderSubmitted:topic={},key={}",record.topic(),record.key());
        String symbol = record.key();
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        if(order == null){
            return ;
        }
        CoinTrader trader = traderFactory.getTrader(symbol);
        //如果当前币种交易暂停会自动取消订单
        if(trader.isTradingHalt() || !trader.getReady()){
            //撮合器未准备完成，撤回当前等待的订单
            kafkaTemplate.send("exchange-order-cancel-success",order.getSymbol(), JSON.toJSONString(order));
        }
        else{
            try {
                long startTick = System.currentTimeMillis();
                trader.trade(order);
                log.info("complete trade,{}ms used!",System.currentTimeMillis() - startTick);
            }
            catch (Exception e){
                e.printStackTrace();
                log.error("====交易出错，退回订单===");
                kafkaTemplate.send("exchange-order-cancel-success",order.getSymbol(), JSON.toJSONString(order));
            }
        }
    }

    @KafkaListener(topics = "exchange-order-cancel",group = "group-handle")
    public void onOrderCancel(ConsumerRecord<String,String> record){
        log.info("onOrderCancel:topic={},accessKey={}",record.topic(),record.key());
        String symbol = record.key();
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        if(order == null){
            return ;
        }
        CoinTrader trader = traderFactory.getTrader(symbol);
        if(trader.getReady()) {
            try {
                ExchangeOrder result = trader.cancelOrder(order);
                if (result != null) {
                    kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(result));
                }
            }
            catch (Exception e){
                log.error("====取消订单出错===");
                e.printStackTrace();
            }
        }
    }
}
