package org.sfu.p2startercode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
public class SecurityConfiguration {
    //DO NOT TOUCH THIS CODE - YOU WILL HAVE A BAD TIME
    //This is required to bypass the default Spring Security which is extremely annoying
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().disable().and()
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(toH2Console())
                        .disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

}