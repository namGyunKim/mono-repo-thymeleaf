package com.example.global.config.security;

import com.example.global.security.SecurityPublicPaths;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import com.example.global.security.filter.support.JsonBodyLoginErrorWriter;
import com.example.global.security.filter.support.JsonBodyLoginRequestParser;
import com.example.global.security.filter.support.JsonBodyLoginRequestValidator;
import com.example.global.security.handler.*;
import com.example.global.security.service.query.PrincipalDetailsQueryService;
import com.example.global.utils.RequestUriUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    /**
     * JSON 로그인 API 경로
     */
    private static final String AUTH_LOGIN_JSON_API_PATH = "/api/sessions";

    private static boolean isJsonLoginApiRequest(final HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String path = RequestUriUtils.getPathWithinApplication(request);
        return path.equals(AUTH_LOGIN_JSON_API_PATH);
    }

    private static boolean isLogoutRequest(final HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String method = request.getMethod();
        if (method == null || !"DELETE".equalsIgnoreCase(method)) {
            return false;
        }

        return isJsonLoginApiRequest(request);
    }

    private static boolean isJsonLoginApiAuthenticationRequest(final HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String method = request.getMethod();
        if (method == null || !"POST".equalsIgnoreCase(method)) {
            return false;
        }

        return isJsonLoginApiRequest(request);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            final PrincipalDetailsQueryService principalDetailsService,
            final PasswordEncoder passwordEncoder
    ) {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(principalDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity http,
            final DaoAuthenticationProvider daoAuthenticationProvider,
            final CustomAuthSuccessHandler successHandler, final CustomAuthFailureHandler failureHandler,
            final CustomAccessDeniedHandler accessDeniedHandler, final CustomAuthenticationEntryPoint entryPoint,
            final JsonBodyLoginRequestParser parser, final JsonBodyLoginRequestValidator validator,
            final JsonBodyLoginErrorWriter errorWriter, final AuthenticationConfiguration authConfig,
            final RoleBasedLogoutSuccessHandler logoutSuccessHandler
    ) throws Exception {
        configureBasePolicy(http);

        final var loginFilter = createJsonBodyLoginFilter(parser, validator, errorWriter, authConfig, successHandler, failureHandler);
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http.logout(logout -> logout
                .logoutRequestMatcher(SecurityConfig::isLogoutRequest)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessHandler(logoutSuccessHandler)
        );

        http.exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(entryPoint));
        http.authenticationProvider(daoAuthenticationProvider);

        return http.build();
    }

    private void configureBasePolicy(final HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(SecurityPublicPaths.PUBLIC_URLS).permitAll()
                .requestMatchers(SecurityPublicPaths.PUBLIC_API_URLS).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
        );
        // API 경로는 CSRF 비활성화 (JSON API는 CSRF 불필요), Thymeleaf 폼은 CSRF 활성화
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private JsonBodyLoginAuthenticationFilter createJsonBodyLoginFilter(
            final JsonBodyLoginRequestParser parser,
            final JsonBodyLoginRequestValidator validator,
            final JsonBodyLoginErrorWriter errorWriter,
            final AuthenticationConfiguration authConfig,
            final CustomAuthSuccessHandler successHandler,
            final CustomAuthFailureHandler failureHandler
    ) throws Exception {
        final JsonBodyLoginAuthenticationFilter filter = new JsonBodyLoginAuthenticationFilter(parser, validator, errorWriter);
        filter.setAuthenticationManager(authConfig.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        filter.setRequiresAuthenticationRequestMatcher(SecurityConfig::isJsonLoginApiAuthenticationRequest);
        return filter;
    }

}
