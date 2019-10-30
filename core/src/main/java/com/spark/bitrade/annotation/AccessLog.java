package com.spark.bitrade.annotation;


import com.spark.bitrade.constant.AdminModule;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLog {
    String operation();
    AdminModule module();
}

