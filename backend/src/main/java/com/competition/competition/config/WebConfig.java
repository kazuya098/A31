package com.competition.competition.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;

/**
 * Web 相关配置。
 * 【需填充】：
 * - CORS：若前端与后端不同源，在 addCorsMappings 里配置允许的 origin、method、header。
 * - WebClient：用于调用算法推理服务的响应式 HTTP 客户端（替代 RestTemplate，性能更好），已配置连接/响应超时。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径 "/" 重定向到 "/api/health"
        registry.addRedirectViewController("/", "/api/health");

        // 或者返回一个简单的欢迎信息
        // registry.addViewController("/").setViewName("forward:/api/health");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    /**
     * 算法服务用的 WebClient：连接与响应超时 30 秒，供 AlgorithmClientService 调用推理接口。
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthTokenStore tokenStore) {
        FilterRegistrationBean<AuthFilter> reg = new FilterRegistrationBean<>(new AuthFilter(tokenStore));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射/uploads/目录到/static/uploads/路径，支持远程访问图片
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("file:./uploads/");

        // 处理 favicon.ico 请求
        registry.addResourceHandler("/**.ico", "/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new ResourceResolver() {
                    @Override
                    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
                        Resource resolved = chain.resolveResource(request, requestPath, locations);
                        return resolved != null ? resolved : new ClassPathResource("static/empty.ico");
                    }

                    @Override
                    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
                        String path = chain.resolveUrlPath(resourcePath, locations);
                        return path != null ? path : "/static/empty.ico";
                    }
                });
    }
}
