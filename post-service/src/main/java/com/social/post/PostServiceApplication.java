package com.social.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class PostServiceApplication {
    public static void main(String[] args) { SpringApplication.run(PostServiceApplication.class, args); }

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() { return RestClient.builder(); }
}
