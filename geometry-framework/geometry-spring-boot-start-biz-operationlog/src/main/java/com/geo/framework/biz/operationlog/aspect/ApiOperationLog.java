package com.geo.framework.biz.operationlog.aspect;


/*
creator：AZERL7
createTime：17:48
*/

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApiOperationLog {
    String description() default "";
}
