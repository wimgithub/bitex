package com.spark.bitrade.config;

import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.util.IdWorkByTwitter;
import com.sparkframework.sql.DB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * @author Zhang Jinwei
 * @date 2017年12月22日
 */
@Configuration
public class SystemConfig {

    @Bean
    public IdWorkByTwitter idWorkByTwitter(@Value("${spark.system.work-id:0}")long workId,@Value("${spark.system.data-center-id:0}")long dataCenterId){
        return new IdWorkByTwitter(workId, dataCenterId);
    }

    @Bean
    public DB db(@Qualifier("dataSource") DataSource dataSource){
        return new DB(dataSource, true);
    }

}
