package com.backend.geopacking.config;

import com.backend.geopacking.auth.jwt.JwtAuthenticationFilter;
import com.backend.geopacking.exceptions.CustomAccessDeniedHandler;
import com.backend.geopacking.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login","/auth/registerAdmin","/auth/forgot-password","/auth/reset-password","/admin/registerClient","/payments-webhook/**","/plans/listPlans").permitAll()
                        .requestMatchers("/auth/profile").authenticated()
                        .requestMatchers(HttpMethod.DELETE,"/client/**").hasAnyAuthority("ADMIN_ACCESS", "CLIENT_ACCESS", "MANAGER_ACCESS")
                        .requestMatchers("/client/**").hasAnyAuthority("ADMIN_ACCESS", "CLIENT_ACCESS", "MANAGER_ACCESS","STAFF_ACCESS")
                        .requestMatchers(HttpMethod.DELETE,"/instructor/**", "/class-sessions/**","/rooms/**", "/disciplines/**").hasAnyAuthority("ADMIN_ACCESS", "INSTRUCTOR_ACCESS", "MANAGER_ACCESS")
                        .requestMatchers("/instructor/**", "/class-sessions/**","/rooms/**", "/disciplines/**").hasAnyAuthority("ADMIN_ACCESS", "INSTRUCTOR_ACCESS", "MANAGER_ACCESS","STAFF_ACCESS", "CLIENT_ACCESS")
                        .requestMatchers(HttpMethod.DELETE,"/admin/**",
                                "/rol/**",
                                "/staff/**",
                                "/manager/**",
                                "/permission/**",
                                "/plans/**",
                                "/payments/current-month-income").hasAnyAuthority("ADMIN_ACCESS", "MANAGER_ACCESS")
                        .requestMatchers("/excel/**").hasAnyAuthority("ADMIN_ACCESS", "MANAGER_ACCESS")
                        .requestMatchers("/admin/**",
                                "/rol/**",
                                "/staff/**",
                                "/manager/**",
                                "/permission/**",
                                "/plans/**",
                                "/payments/current-month-income",
                                "/attendance").hasAnyAuthority("ADMIN_ACCESS", "MANAGER_ACCESS","STAFF_ACCESS")
                        .requestMatchers("/payments/**").hasAuthority("CLIENT_ACCESS")
                        .requestMatchers("/rooms/**").hasAuthority("ROOM_ACCESS")
                        .requestMatchers("/disciplines/**").hasAuthority("DISCIPLINE_ACCESS")
                        .requestMatchers("/rol/**").hasAuthority("ROLE_ACCESS")
                        .requestMatchers("/class-sessions/**").hasAuthority("CLASS_SESSION_ACCESS")
                        .requestMatchers("/excel/**").hasAuthority("EXCEL_ACCESS")
                        .requestMatchers("/payments/**").hasAuthority("PAYMENT_ACCESS")
                        .anyRequest().authenticated()
                ).exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
