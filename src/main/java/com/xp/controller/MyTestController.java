package com.xp.controller;

import com.xp.annoation.XpAtuowired;
import com.xp.annoation.XpController;
import com.xp.annoation.XpRequestMapping;
import com.xp.annoation.XpRequestParam;
import com.xp.service.MyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
@XpController
@XpRequestMapping("/xp")
public class MyTestController {

    @XpAtuowired("myService")
    private MyService myService;

    @XpRequestMapping("/test")
    public void test(@XpRequestParam("name") String name, HttpServletResponse response, @XpRequestParam("age") String age) {
        String str = myService.query(name, age);
        System.out.println(str);

        try {
            PrintWriter out = response.getWriter();
            out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
