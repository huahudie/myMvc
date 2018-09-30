package com.xp.service;

import com.xp.annoation.XpService;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
@XpService("myService")
public class MyServiceImpl implements MyService {

    public String query(String name, String age) {
        return "my name is " + name + ", age is " + age;
    }
}
