package com.skylerdache.spacepong.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * MAGIC BOILERPLATE
 * No clue why this code needs to exist, but the following source tells me this is necessary
 * https://zetcode.com/spring/websocket/
 */
public class MyWebInitializer extends
        AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class,WebSocketConfig.class};
    }

    @Override
    protected String @NotNull [] getServletMappings() {
        return new String[]{"/"};
    }
}
