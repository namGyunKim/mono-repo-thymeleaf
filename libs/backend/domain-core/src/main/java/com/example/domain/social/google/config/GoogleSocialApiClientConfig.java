package com.example.domain.social.google.config;

import com.example.domain.social.google.client.GoogleApiClient;
import com.example.domain.social.google.client.GoogleOauthClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GoogleSocialApiClientConfig {

    /**
     * 공통 {@link RestClient.Builder} 빈을 생성합니다.
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Google API 호출용 {@link RestClient} 빈을 생성합니다. (baseUrl: {@code social.google.apiBaseUrl})
     */
    @Bean
    public RestClient googleApiRestClient(final RestClient.Builder restClientBuilder, @Value("${social.google.apiBaseUrl}") final String baseUrl) {
        return restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Google OAuth 토큰 폐기용 {@link RestClient} 빈을 생성합니다. (baseUrl: {@code social.google.revokeBaseUrl})
     */
    @Bean
    public RestClient googleOauthRestClient(final RestClient.Builder restClientBuilder, @Value("${social.google.revokeBaseUrl:https://oauth2.googleapis.com}") final String baseUrl) {
        return restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Google 사용자 정보 조회 HTTP 인터페이스 클라이언트 빈을 생성합니다.
     */
    @Bean
    public GoogleApiClient googleApiClient(final RestClient googleApiRestClient) {
        return createClient(googleApiRestClient, GoogleApiClient.class);
    }

    /**
     * Google OAuth 토큰 폐기 HTTP 인터페이스 클라이언트 빈을 생성합니다.
     */
    @Bean
    public GoogleOauthClient googleOauthClient(final RestClient googleOauthRestClient) {
        return createClient(googleOauthRestClient, GoogleOauthClient.class);
    }

    private <T> T createClient(final RestClient restClient, final Class<T> clientType) {
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(clientType);
    }
}
