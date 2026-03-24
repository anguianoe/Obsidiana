package com.nexcoyo.knowledge.obsidiana.config;

import com.nexcoyo.knowledge.obsidiana.filter.AuthJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String API = "/api/v1";
    private static final String API_ADMIN = API + "/admin";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthJwtFilter authJwtFilter) {

        try{
            http
                    .cors( Customizer.withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    PathPatternRequestMatcher.withDefaults().matcher("/"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/index.html"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/favicon.ico"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/favicon.png"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/logo.png"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/user.png"),

                                    PathPatternRequestMatcher.withDefaults().matcher("/assets/**"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/media/**"),

                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.js"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.css"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.map"),

                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.png"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.jpg"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.jpeg"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.svg"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.webp"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.ico"),

                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.woff2"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.woff"),
                                    PathPatternRequestMatcher.withDefaults().matcher("/**/*.ttf")
                            ).permitAll()
                            .requestMatchers( HttpMethod.GET, "/actuator/health").permitAll()
                            .requestMatchers(API + "/auth/**").permitAll()
                            .requestMatchers(
                                    API_ADMIN + "/users/**",
                                    API_ADMIN + "/workspaces/**",
                                    API_ADMIN + "/tags/**"
                            ).hasRole( "SUPER_ADMIN" )
                            .requestMatchers(
                                    API + "/workspaces/**",
                                    API + "/tags/**"
                            ).hasRole( "USER" )
                            /*

                            // ✅ ADMIN o USER
                            .requestMatchers(
                                    API + "/user-actions/**",
                                    API + "/engineer/**",
                                    API + "/orchard/**",
                                    API + "/suppliers/**",
                                    API + "/excel-files/**",
                                    API + "/runs/**",
                                    API + "/reports/**"
                            ).hasAnyRole("ADMIN", "USER")
                            //.requestMatchers(API + "/**").authenticated()*/
                            .anyRequest().permitAll()
                    )
                    .addFilterBefore(authJwtFilter, UsernamePasswordAuthenticationFilter.class);

                    return http.build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error in SecurityConfig", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
