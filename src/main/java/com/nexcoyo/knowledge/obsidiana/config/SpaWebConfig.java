package com.nexcoyo.knowledge.obsidiana.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer
{

    @Override
    public void addViewControllers( ViewControllerRegistry registry) {

        // Forward routes SPA (sin extensión) para single-segment (se mantiene como respaldo)
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");

        // El mapeo con '/**/{path:[^\\.]*}' se elimina porque Spring PathPatternParser no permite
        // '**' seguido de más datos de patrón (provocaba PatternParseException).
    }
}