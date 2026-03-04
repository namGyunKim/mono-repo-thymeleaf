package com.example.global.security.filter.support;

import com.example.domain.account.payload.request.AccountUserLoginRequest;
import com.example.global.payload.response.ApiErrorDetail;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JsonBodyLoginRequestParser {

    private final ObjectMapper objectMapper;

    public boolean isJsonRequest(final HttpServletRequest request) {
        final String contentType = request != null ? request.getContentType() : null;
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    public LoginRequestParseResult parse(final HttpServletRequest request) {
        if (request == null) {
            return LoginRequestParseResult.from(
                    List.of(ApiErrorDetail.of("body", "요청 값이 비어있습니다."))
            );
        }

        try (final InputStream inputStream = request.getInputStream()) {
            return LoginRequestParseResult.of(objectMapper.readValue(inputStream, AccountUserLoginRequest.class));
        } catch (final JacksonException e) {
            return LoginRequestParseResult.from(
                    List.of(ApiErrorDetail.of("body", "요청 본문(JSON) 파싱에 실패했습니다."))
            );
        } catch (final IOException e) {
            return LoginRequestParseResult.from(
                    List.of(ApiErrorDetail.of("body", "요청 본문을 읽는 중 오류가 발생했습니다."))
            );
        }
    }
}
