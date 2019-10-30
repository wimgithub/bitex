package com.spark.bitrade.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreExcel {

    boolean value() default true ;
}
