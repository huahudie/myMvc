package com.xp.annoation;

import java.lang.annotation.*;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XpAtuowired {
    String value() default "";
}
