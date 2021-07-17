package com.demo.aop.advisor;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

@Component
public class DemoBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
//        System.out.println(method.getName());
    }
}
