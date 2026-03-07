package com.example.global.security.filter.support;

import com.example.domain.account.payload.request.AccountUserLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonBodyLoginRequestParserTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonBodyLoginRequestParser parser;

    // === isJsonRequest ===

    @Nested
    @DisplayName("isJsonRequest")
    class IsJsonRequest {

        @Test
        @DisplayName("application/json Content-Type이면 true 반환")
        void isJsonRequest_applicationJson_returnsTrue() {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContentType("application/json");

            // Act
            final boolean result = parser.isJsonRequest(request);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("application/json;charset=UTF-8 Content-Type이면 true 반환")
        void isJsonRequest_applicationJsonWithCharset_returnsTrue() {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContentType("application/json;charset=UTF-8");

            // Act
            final boolean result = parser.isJsonRequest(request);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("대소문자 무관하게 application/json 포함 시 true 반환")
        void isJsonRequest_mixedCase_returnsTrue() {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContentType("Application/JSON");

            // Act
            final boolean result = parser.isJsonRequest(request);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("text/html Content-Type이면 false 반환")
        void isJsonRequest_textHtml_returnsFalse() {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContentType("text/html");

            // Act
            final boolean result = parser.isJsonRequest(request);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Content-Type이 null이면 false 반환")
        void isJsonRequest_nullContentType_returnsFalse() {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();

            // Act
            final boolean result = parser.isJsonRequest(request);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null request이면 false 반환")
        void isJsonRequest_nullRequest_returnsFalse() {
            // Act
            final boolean result = parser.isJsonRequest(null);

            // Assert
            assertThat(result).isFalse();
        }
    }

    // === parse ===

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("null request 전달 시 에러가 포함된 실패 결과 반환")
        void parse_nullRequest_returnsFailure() {
            // Act
            final LoginRequestParseResult result = parser.parse(null);

            // Assert
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.loginRequest()).isNull();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().field()).isEqualTo("body");
        }

        @Test
        @DisplayName("일반 사용자 경로로 정상 JSON 파싱 시 AccountUserLoginRequest 반환")
        void parse_userLoginPath_returnsUserLoginRequest() throws Exception {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/sessions");
            request.setContent("{\"loginId\":\"user01\",\"password\":\"1234\"}".getBytes(StandardCharsets.UTF_8));

            final AccountUserLoginRequest expectedRequest = AccountUserLoginRequest.of("user01", "1234");
            when(objectMapper.readValue(any(InputStream.class), eq(AccountUserLoginRequest.class)))
                    .thenReturn(expectedRequest);

            // Act
            final LoginRequestParseResult result = parser.parse(request);

            // Assert
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.loginRequest()).isEqualTo(expectedRequest);
        }

        @Test
        @DisplayName("잘못된 JSON 파싱 시 JacksonException에 대한 실패 결과 반환")
        void parse_invalidJson_returnsFailureWithParseError() throws Exception {
            // Arrange
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/sessions");
            request.setContent("invalid json".getBytes(StandardCharsets.UTF_8));

            when(objectMapper.readValue(any(InputStream.class), eq(AccountUserLoginRequest.class)))
                    .thenThrow(org.mockito.Mockito.mock(JacksonException.class));

            // Act
            final LoginRequestParseResult result = parser.parse(request);

            // Assert
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.loginRequest()).isNull();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().reason()).contains("파싱");
        }

        @Test
        @DisplayName("IO 오류 발생 시 실패 결과 반환")
        void parse_ioException_returnsFailureWithIoError() {
            // Arrange: getInputStream()에서 IOException이 발생하는 요청을 시뮬레이션
            final HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
            try {
                when(request.getInputStream()).thenThrow(new IOException("읽기 실패"));
            } catch (final IOException ignored) {
                // mock 설정 시 발생하지 않음
            }

            // Act
            final LoginRequestParseResult result = parser.parse(request);

            // Assert
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.loginRequest()).isNull();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().reason()).contains("오류");
        }
    }
}
