package com.skylerdache.spacepong.configuration;

import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MAGIC BOILERPLATE
 * No clue why this code needs to exist, but the following source tells me this is necessary
 * https://zetcode.com/spring/websocket/
 */
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
