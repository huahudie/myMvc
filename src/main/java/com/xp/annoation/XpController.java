package com.xp.annoation;

import java.lang.annotation.*;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XpController {
    String value() default "";
}
