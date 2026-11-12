package com.rollwave.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var h2Console = PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(h2Console).permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers(h2Console))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
