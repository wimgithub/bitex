package com.spark.bitrade.config;

import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.support.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsProviderConfig {

    @Value("${sms.gateway:}")
    private String gateway;
    @Value("${sms.username:}")
    private String username;
    @Value("${sms.password:}")
    private String password;
    @Value("${sms.sign:}")
    private String sign;
    @Value("${sms.internationalGateway:}")
    private String internationalGateway;
    @Value("${sms.internationalUsername:}")
    private String internationalUsername;
    @Value("${sms.internationalPassword:}")
    private String internationalPassword;
    @Value("${sms.apiKey:}")
    private String apiKey;
    @Value("${sms.tpl_id:}")
    private String tplId;
    @Value("${sms.key.id:}")
    private String accessKey;
    @Value("${sms.key.secret:}")
    private String accessSecret;


    @Bean
    public SMSProvider getSMSProvider(@Value("${sms.driver:}") String driverName) {
        if (StringUtils.isEmpty(driverName)||driverName.equalsIgnoreCase(ChuangRuiSMSProvider.getName())) {
            return new ChuangRuiSMSProvider(gateway, username, password, sign);
        } else if (driverName.equalsIgnoreCase(EmaySMSProvider.getName())) {
            return new EmaySMSProvider(gateway, username, password);
        }else if (driverName.equalsIgnoreCase(HuaXinSMSProvider.getName())) {
            return new HuaXinSMSProvider(gateway, username, password,internationalGateway,internationalUsername,internationalPassword,sign);
        } else if(driverName.equalsIgnoreCase(TwoFiveThreeProvider.getName())){
            return new TwoFiveThreeProvider(gateway,username,password,sign);
        } else if(driverName.equalsIgnoreCase(YunpianSMSProvider.getName())){
            return new YunpianSMSProvider(gateway,apiKey,sign,tplId);
        } else if(driverName.equalsIgnoreCase(ChuangRuiYunSMSProvider.getName())){
            return new ChuangRuiYunSMSProvider(gateway,accessKey,accessSecret,sign,tplId);
        } else {
            return null;
        }
    }
}
