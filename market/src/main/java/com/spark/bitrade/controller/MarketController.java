package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.ExchangeTradeService;
import com.spark.bitrade.service.LeverCoinService;
import com.spark.bitrade.service.MarketService;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private ExchangeCoinService coinService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private ExchangeTradeService exchangeTradeService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LeverCoinService leverCoinService;

    public static final String SERVICE_NAME = "http://service-exchange-trade";

    /**
     * 获取支持的交易币种
     * @return
     */
    @RequestMapping("symbol")
    public List<ExchangeCoin> findAllSymbol(){
        return coinService.findAllEnabled();
    }

    @RequestMapping("overview")
    public Map<String,List<CoinThumb>> overview(){
        log.info("/market/overview");
        Map<String,List<CoinThumb>> result = new HashMap<>();
        List<ExchangeCoin> recommendCoin = coinService.findAllByFlag(1);
        List<CoinThumb> recommendThumbs = new ArrayList<>();
        madeThumbs(recommendCoin,recommendThumbs);
        result.put("recommend",recommendThumbs);
        List<CoinThumb> allThumbs = findSymbolThumb(false);
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));
        int limit = allThumbs.size() > 5 ? 5 : allThumbs.size();
        result.put("changeRank",allThumbs.subList(0,limit));
        return result;
    }


    /**
     * 获取某交易对详情
     * @param symbol
     * @return
     */
    @RequestMapping("symbol-info")
    public ExchangeCoin findSymbol(String symbol){
        return coinService.findBySymbol(symbol);
    }

    /**
     * 获取币种缩略行情
     * @return
     */
    @RequestMapping("symbol-thumb")
    public List<CoinThumb> findSymbolThumb(Boolean isLever){
        List<CoinThumb> thumbs = new ArrayList<>();
        if(isLever!=null&&isLever){
            List<LeverCoin> leverCoinList=leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
            for(LeverCoin leverCoin:leverCoinList){
                CoinProcessor processor=coinProcessorFactory.getProcessor(leverCoin.getSymbol());
                if(processor != null) {
                    CoinThumb thumb = processor.getThumb();
                    thumbs.add(thumb);
                }
            }
        }else{
            List<ExchangeCoin> coins = coinService.findAllEnabled();
            madeThumbs(coins,thumbs);
        }
        return thumbs;
    }

    @RequestMapping("symbol-thumb-trend")
    public JSONArray findSymbolThumbWithTrend(){
        List<ExchangeCoin> coins = coinService.findAllEnabled();
        //List<CoinThumb> thumbs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.set(Calendar.MINUTE,0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY,-24);
        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();
        for(ExchangeCoin coin:coins){
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            JSONObject json = (JSONObject) JSON.toJSON(thumb);
            json.put("zone",coin.getZone());
            List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(),firstTimeOfToday,nowTime,"1hour");
            JSONArray trend = new JSONArray();
            for(KLine line:lines){
                trend.add(line.getClosePrice());
            }
            json.put("trend",trend);
            array.add(json);
        }
        return array;
    }

    /**
     * 获取币种历史K线
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @RequestMapping("history")
    public JSONArray findKHistory(String symbol,long from,long to,String resolution){
        String period = "";
        if(resolution.endsWith("H") || resolution.endsWith("h")){
            period = resolution.substring(0,resolution.length()-1) + "hour";
        }
        else if(resolution.endsWith("D") || resolution.endsWith("d")){
            period = resolution.substring(0,resolution.length()-1) + "day";
        }
        else if(resolution.endsWith("W") || resolution.endsWith("w")){
            period = resolution.substring(0,resolution.length()-1) + "week";
        }
        else if(resolution.endsWith("M") || resolution.endsWith("m")){
            period = resolution.substring(0,resolution.length()-1) + "month";
        }
        else{
            Integer val = Integer.parseInt(resolution);
            if(val < 60) {
                period = resolution + "min";
            }
            else {
                period = (val/60) + "hour";
            }
        }
        List<KLine> list = marketService.findAllKLine(symbol,from,to,period);

        JSONArray array = new JSONArray();
        for(KLine item:list){
            JSONArray group = new JSONArray();
            group.add(0,item.getTime());
            group.add(1,item.getOpenPrice());
            group.add(2,item.getHighestPrice());
            group.add(3,item.getLowestPrice());
            group.add(4,item.getClosePrice());
            group.add(5,item.getVolume());
            array.add(group);
        }
        return array;
    }

    /**
     * 查询最近成交记录
     * @param symbol 交易对符号
     * @param size 返回记录最大数量
     * @return
     */
    @RequestMapping("latest-trade")
    public List<ExchangeTrade> latestTrade(String symbol, int size){
        //ExchangeCoin exchangeCoin = coinService.findBySymbol(symbol);
        return exchangeTradeService.findLatest(symbol,size);
    }

    @RequestMapping("exchange-plate")
    public Map<String,List<TradePlateItem>> findTradePlate(String symbol){
        //远程RPC服务URL,后缀为币种单位
        String url = SERVICE_NAME + "/monitor/plate?symbol="+symbol;
        log.info("url={}",url);
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String, List<TradePlateItem>>) result.getBody();
    }


    @RequestMapping("exchange-plate-mini")
    public Map<String,JSONObject> findTradePlateMini(String symbol){
        //远程RPC服务URL,后缀为币种单位
        String url = SERVICE_NAME + "/monitor/plate-mini?symbol="+symbol;
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String, JSONObject>) result.getBody();
    }


    @RequestMapping("exchange-plate-full")
    public Map<String,JSONObject> findTradePlateFull(String symbol){
        //远程RPC服务URL,后缀为币种单位
        String url = SERVICE_NAME + "/monitor/plate-full?symbol="+symbol;
        ResponseEntity<HashMap> result = restTemplate.getForEntity(url, HashMap.class);
        return (Map<String,JSONObject>) result.getBody();
    }

    private void madeThumbs(List<ExchangeCoin> coins,List<CoinThumb> thumbs){
        for(ExchangeCoin coin:coins){
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if(processor != null) {
                CoinThumb thumb = processor.getThumb();
                thumbs.add(thumb);
            }
        }
    }
}
