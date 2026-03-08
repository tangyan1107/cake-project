package com.cake.annotation;

import com.cake.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *自动填充注解，用于标识某个方法，需要进行公共字段的自动填充处理
 */
@Target(ElementType.METHOD)//指定注解只能加在方法上面
@Retention(RetentionPolicy.RUNTIME)//指定注解在方法运行时运行
public @interface AutoFill {
    //数据库操作类型：UPDATE INSERT
    OperationType value();
}
