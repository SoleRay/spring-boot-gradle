package com.demo.config.filter;

import com.demo.filter.DemoFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Arthur on 2017/7/18 0018.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean demoFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DemoFilter());
        registration.addUrlPatterns("/demo/*");
        registration.setName("demo");
        registration.setOrder(1);
        return registration;
    }

}
