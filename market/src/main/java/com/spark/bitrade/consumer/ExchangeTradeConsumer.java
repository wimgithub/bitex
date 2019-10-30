package com.spark.bitrade.consumer;


import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.NettyCommand;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.handler.NettyHandler;
import com.spark.bitrade.job.ExchangePushJob;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.ExchangeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
public class ExchangeTradeConsumer {
    private Logger logger = LoggerFactory.getLogger(ExchangeTradeConsumer.class);
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private NettyHandler nettyHandler;
    @Value("${second.referrer.award}")
    private boolean secondReferrerAward;
    private ExecutorService executor = Executors.newFixedThreadPool(30);
    @Autowired
    private ExchangePushJob pushJob;


    /**
     * 处理成交明细
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade", group = "group-handle")
    public void handleTrade(ConsumerRecord<String, String> record) {
        logger.info("topic={},key={},value={}", record.topic(), record.key(), record.value());
        long startTick = System.currentTimeMillis();
        String symbol = record.key();
        List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
        //处理K线行情
        CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
        if (coinProcessor != null) {
            coinProcessor.process(trades);
        }
        pushJob.addTrades(symbol, trades);
        executor.submit(new HandleTradeThread(record));
        log.info("complete exchange process,{}ms used!",System.currentTimeMillis() - startTick);
    }


    @KafkaListener(topics = "exchange-order-completed", group = "group-handle")
    public void handleOrderCompleted(ConsumerRecord<String, String> record) {
        logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
        String symbol = record.key();
        try {
            List<ExchangeOrder> orders = JSON.parseArray(record.value(), ExchangeOrder.class);
            for (ExchangeOrder order : orders) {
                //委托成交完成处理
                exchangeOrderService.orderCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                //推送订单成交
                messagingTemplate.convertAndSend("/topic/market/order-completed/" + symbol + "/" + order.getMemberId(), order);
                nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_COMPLETED, order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理模拟交易
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-mocker", group = "group-handle")
    public void handleMockerTrade(ConsumerRecord<String, String> record) {
        logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
        try {
            List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
            String symbol = record.key();
            //处理行情
            CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
            if (coinProcessor != null) {
                coinProcessor.process(trades);
            }
            pushJob.addTrades(symbol,trades);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消费交易盘口信息
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trade-plate")
    public void handleTradePlate(ConsumerRecord<String, String> record) {
        try {
            logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
            String symbol = record.key();
            TradePlate plate = JSON.parseObject(record.value(), TradePlate.class);
            pushJob.addPlates(symbol,plate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订单取消成功
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-order-cancel-success")
    public void handleOrderCanceled(ConsumerRecord<String, String> record) {
        try {
            logger.info("topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
            String symbol = record.key();
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            //调用服务处理
            exchangeOrderService.orderCanceled(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
            //推送实时成交
            messagingTemplate.convertAndSend("/topic/market/order-canceled/" + symbol + "/" + order.getMemberId(), order);
            nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_CANCELED, order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class HandleTradeThread  implements Runnable{
        private ConsumerRecord<String, String> record;
        private HandleTradeThread(ConsumerRecord<String, String> record){
           this.record = record;
        }
        @Override
        public void run() {
            try {
                List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
                String symbol = record.key();
                for (ExchangeTrade trade : trades) {
                    //成交明细处理
                    exchangeOrderService.processExchangeTrade(trade, secondReferrerAward);
                    //推送订单成交订阅
                    ExchangeOrder buyOrder = exchangeOrderService.findOne(trade.getBuyOrderId());
                    ExchangeOrder sellOrder = exchangeOrderService.findOne(trade.getSellOrderId());
                    messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + buyOrder.getMemberId(), buyOrder);
                    messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + sellOrder.getMemberId(), sellOrder);
                    nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, buyOrder);
                    nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, sellOrder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
