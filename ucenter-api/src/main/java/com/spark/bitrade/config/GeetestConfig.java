package com.spark.bitrade.config;

import com.spark.bitrade.system.GeetestLib;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeetestConfig {
    @Value("${geetest.captchaId}")
    private String captchaId;
    @Value("${geetest.privateKey}")
    private String privateKey;
    @Value("${geetest.newFailback}")
    private int newFailback;

    @Bean
    public GeetestLib geetestLib() {
        return new GeetestLib(captchaId, privateKey, newFailback == 0 ? false : true);
    }
}
