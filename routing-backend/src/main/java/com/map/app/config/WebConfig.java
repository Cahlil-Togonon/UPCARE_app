package com.map.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Siftee
 */
@Configuration
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // This means all endpoints will be CORS-enabled
            .allowedOrigins("http://localhost:3000") // The origin you want to allow
            .allowedMethods("GET", "POST", "PUT", "DELETE") // Methods you want to allow
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true); // Allow credentials like cookies or authorization headers
}

@Override
public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

    configurer.defaultContentType(MediaType.ALL).favorParameter(true).parameterName("mediaType").
    mediaType("json", MediaType.APPLICATION_JSON);
  }
}
