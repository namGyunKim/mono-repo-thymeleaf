package com.example.domain.log.support;

import com.example.global.payload.response.ApiErrorDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorSerializerTest {

    @Test
    void serialize_valid_list_returns_json() {
        final List<ApiErrorDetail> errors = List.of(
                ApiErrorDetail.of("email", "필수 입력"),
                ApiErrorDetail.of("age", "0보다 커야 합니다")
        );

        final String result = ValidationErrorSerializer.serialize(errors);

        assertThat(result).isEqualTo(
                "[{\"field\":\"email\",\"reason\":\"필수 입력\"},{\"field\":\"age\",\"reason\":\"0보다 커야 합니다\"}]"
        );
    }

    @Test
    void serialize_empty_list_returns_null() {
        final String result = ValidationErrorSerializer.serialize(List.of());

        assertThat(result).isNull();
    }

    @Test
    void serialize_null_returns_null() {
        final String result = ValidationErrorSerializer.serialize(null);

        assertThat(result).isNull();
    }

    @Test
    void serialize_special_characters_escapes_correctly() {
        final List<ApiErrorDetail> errors = List.of(
                ApiErrorDetail.of("field\"name", "reason with \"quotes\" and \\backslash")
        );

        final String result = ValidationErrorSerializer.serialize(errors);

        assertThat(result).contains("field\\\"name");
        assertThat(result).contains("reason with \\\"quotes\\\" and \\\\backslash");
    }

    @Test
    void serialize_single_item_returns_json_array() {
        final List<ApiErrorDetail> errors = List.of(
                ApiErrorDetail.of("name", "필수")
        );

        final String result = ValidationErrorSerializer.serialize(errors);

        assertThat(result).isEqualTo("[{\"field\":\"name\",\"reason\":\"필수\"}]");
    }
}
