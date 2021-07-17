package com.demo.aop.advisor;


import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoAdvisor {

    @Bean
    public Advisor getAdvisor(DemoBeforeAdvice demoBeforeAdvice){
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setAdvice(demoBeforeAdvice);
//        advisor.setExpression("execution(public * com.demo.controller..*(..)) && @annotation(com.demo.anno.SignCheck)");
        advisor.setExpression("execution(public * com.demo.controller..*(..))");
        return advisor;
    }
}
