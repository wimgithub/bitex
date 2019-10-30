package com.spark.bitrade.job;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.component.CoinExchangeRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LegalRateSyncJob {
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    /**
     * 每小时同步一次价格
     *
     * @throws UnirestException
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sync(){
        try {
            coinExchangeRate.syncLegalRate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
