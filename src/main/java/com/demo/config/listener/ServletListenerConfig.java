package com.demo.config.listener;

import com.demo.listener.DemoServletListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletListenerConfig {

    @Bean
    public ServletListenerRegistrationBean registerDemoListener(){
        ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
        servletListenerRegistrationBean.setListener(new DemoServletListener());
        return servletListenerRegistrationBean;
    }
}
