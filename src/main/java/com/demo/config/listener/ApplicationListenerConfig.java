package com.demo.config.listener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;



@Configuration
public class ApplicationListenerConfig {

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {

        System.out.println("event trigged!");
    }
}
