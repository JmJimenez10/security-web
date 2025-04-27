package dev.jmjimenez.security_spring_boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//@Profile("prod")
//public class ProdCorsConfig {
//
//	@Bean
//	public WebMvcConfigurer prodCorsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/api/**").allowedOrigins("https://webprod.com").allowedMethods("GET", "POST", "PUT", "DELETE")
//						.allowedHeaders("Authorization", "Content-Type").allowCredentials(true).maxAge(3600);
//			}
//		};
//	}
//
//}
