package com.competition.competition.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.nio.file.Paths;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

/**
 * Web 相关配置。
 * 【需填充】：
 * - CORS：若前端与后端不同源，在 addCorsMappings 里配置允许的 origin、method、header。
 * - WebClient：用于调用算法推理服务的响应式 HTTP 客户端（替代 RestTemplate，性能更好），已配置连接/响应超时。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径 "/" 重定向到 "/api/health"
        registry.addRedirectViewController("/", "/api/health");

        // 或者返回一个简单的欢迎信息
        // registry.addViewController("/").setViewName("forward:/api/health");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有静态资源的跨域访问（包括上传的图片）
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    /**
     * 算法服务用的 WebClient：连接与响应超时 30 秒，供 AlgorithmClientService 调用推理接口。
     * 增加了内存缓冲区限制到 50MB，以支持算法返回包含 Base64 图片的大响应。
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30));
        
        // 设置内存缓冲区大小为 50MB (默认是 256KB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
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
        // 映射 /api/uploads/** → 本地上传目录。
        // 路径必须带 /api 前缀，使 Vite 代理能将图片请求转发到后端。
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString() + "/");

        // 映射 /api/data/** → Data/Mandrillus/examples 演示图片目录
        registry.addResourceHandler("/api/data/**")
                .addResourceLocations(Paths.get("../Data/Mandrillus/examples").toAbsolutePath().normalize().toUri().toString() + "/");

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
