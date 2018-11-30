package com.xp.dispatch;

import com.xp.annoation.*;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * create by wxp
 *
 * @date 2018-09-21
 */
public class MyDispatchServlet extends HttpServlet {

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> beans = new HashMap<>();

    private Map<String, Object> urlMappings = new HashMap<>();

    private Map<String, Object> urlControllers = new HashMap<>();

    @Override
    public void init() throws ServletException {
        scanPackage("com.xp");
        doInstance();
        doIoc();
        buildMapping();
    }

    /**
     * 扫描注解类
     */
    private void scanPackage(String pack) {
        URL url = this.getClass().getClassLoader().getResource(pack.replaceAll("\\.", "/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        File[] files = file.listFiles();
        for(File f : files) {
            if(f.isDirectory()) {
                scanPackage(pack+"."+f.getName());
            } else {
                classNames.add(pack+"."+f.getName());
            }
        }
    }

    /**
     * 实例化扫描出的类
     */
    private void doInstance() {
        for(String clazz : classNames) {
            clazz = clazz.replace(".class", "");
            try {
                Class<?> cz = Class.forName(clazz);
                if(cz.isAnnotationPresent(XpController.class)) {
                    Object obj = cz.getDeclaredConstructor().newInstance();
                    XpController controller = cz.getDeclaredAnnotation(XpController.class);
                    String key = controller.value();
                    beans.put(key, obj);//把controller类新建一个对象和类名一起放在beans中
                } else if(cz.isAnnotationPresent(XpService.class)) {
                    Object obj = cz.getDeclaredConstructor().newInstance();
                    XpService anno = cz.getDeclaredAnnotation(XpService.class);
                    String key = anno.value();
                    beans.put(key, obj);//新建一个service对象和value值放在beans中
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实现依赖注入
     */
    private void doIoc() {
        if(beans.size() <= 0) {
            System.err.println("没有实例化对象....");
            return;
        }
        for(Object obj : beans.values()) {
            Class<?> clazz = obj.getClass();
            if(clazz.isAnnotationPresent(XpController.class)) {
                Field[] fields = clazz.getDeclaredFields();
                for(Field field : fields) {
                    if(field.isAnnotationPresent(XpAtuowired.class)) {
                        XpAtuowired auto = field.getDeclaredAnnotation(XpAtuowired.class);
                        Object value = beans.get(auto.value());//拿到beans中实例化的service类
                        field.setAccessible(true);
                        try {
                            field.set(obj, value);//将实例化的service类注入到controller类中的service字段中
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 建立url到方法的关系
     */
    private void buildMapping() {
        for(Object obj : beans.values()) {
            Class<?> clazz = obj.getClass();
            if(clazz.isAnnotationPresent(XpController.class)) {
                String url = "";
                if(clazz.isAnnotationPresent(XpRequestMapping.class)) {
                    XpRequestMapping declaredAnnotation = clazz.getDeclaredAnnotation(XpRequestMapping.class);
                    url = declaredAnnotation.value();//拿到类上的url/xp
                }
                Method[] methods = clazz.getDeclaredMethods();
                for(Method m : methods) {
                    if(m.isAnnotationPresent(XpRequestMapping.class)) {
                        XpRequestMapping da = m.getDeclaredAnnotation(XpRequestMapping.class);
                        url = url + da.value();//类上的url组合上方法上的url /xp/test
                        urlMappings.put(url, m);//建立方法与url的map
                        urlControllers.put(url, obj);//建立类实例与url的map
                    }
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        String url = requestURI.replace(contextPath, "");

        Method method = (Method) urlMappings.get(url);//拿到方法
        Object obj = urlControllers.get(url);//拿到类
        if(Objects.nonNull(obj)) {
            Object[] args = this.hand(req, resp, method);
            try {
                method.invoke(obj, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("错误，没有找到[" + url + "]对应的方法...");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    private Object[] hand(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        int args_i = 0;
        int index = 0;
        for(Class<?> paramClazz : parameterTypes) {
            if(ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = req;
            }
            if(ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = resp;//拿到response放入args数组中
            }
            Annotation[] paramAnnos = method.getParameterAnnotations()[index];
            if(paramAnnos.length > 0) {
                for(Annotation param : paramAnnos) {
                    if(XpRequestParam.class.isAssignableFrom(param.getClass())) {
                        XpRequestParam p = (XpRequestParam)param;
                        args[args_i++] = req.getParameter(p.value());//拿到参数name和age依次放入args数组中
                    }
                }
            }
            index++;
        }
        return args;//得到参数数组
    }
}
