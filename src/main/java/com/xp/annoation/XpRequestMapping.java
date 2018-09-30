package com.xp.annoation;

import java.lang.annotation.*;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XpRequestMapping {
    String value() default "";
}
