package com.example.global.security.filter.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.domain.account.payload.request.AccountUserLoginRequest;
import com.example.domain.account.validator.LoginAccountValidator;
import com.example.domain.account.validator.LoginRequestRoleStrategy;
import com.example.global.exception.GlobalException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class JsonBodyLoginRequestValidatorTest {

    @Mock
    private Validator beanValidator;

    @Mock
    private LoginAccountValidator loginAccountValidator;

    @Mock
    private LoginRequestRoleStrategy roleStrategy;

    // === validate ===

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("null 요청 전달 시 에러 포함 결과 반환")
        void validate_nullRequest_returnsErrorResult() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of(roleStrategy));

            // Act
            final LoginRequestValidationResult result = validator.validate(null);

            // Assert
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().field()).isEqualTo("body");
            assertThat(result.loginRequest()).isNull();
        }

        @Test
        @DisplayName("Bean Validation 에러 있을 때 에러 포함 결과 반환")
        @SuppressWarnings("unchecked")
        void validate_beanValidationError_returnsErrorResult() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of(roleStrategy));
            final AccountUserLoginRequest loginRequest = AccountUserLoginRequest.of("", "1234");

            when(roleStrategy.supports(loginRequest.getClass())).thenReturn(true);
            when(roleStrategy.resolveLoginId(loginRequest)).thenReturn("");
            when(roleStrategy.resolvePassword(loginRequest)).thenReturn("1234");

            final ConstraintViolation<AccountUserLoginRequest> violation = createMockViolation("loginId", "로그인 아이디를 입력해주세요.");
            when(beanValidator.validate(loginRequest)).thenReturn(Set.of(violation));

            // Act
            final LoginRequestValidationResult result = validator.validate(loginRequest);

            // Assert
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).isNotEmpty();
            assertThat(result.loginId()).isEmpty();
        }

        @Test
        @DisplayName("Bean Validation 통과 + Custom Validation 에러 없을 때 성공 결과 반환")
        void validate_allValidationsPass_returnsSuccessResult() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of(roleStrategy));
            final AccountUserLoginRequest loginRequest = AccountUserLoginRequest.of("user01", "1234");

            when(roleStrategy.supports(loginRequest.getClass())).thenReturn(true);
            when(roleStrategy.resolveLoginId(loginRequest)).thenReturn("user01");
            when(roleStrategy.resolvePassword(loginRequest)).thenReturn("1234");
            when(beanValidator.validate(loginRequest)).thenReturn(Collections.emptySet());
            when(loginAccountValidator.supports(any())).thenReturn(true);

            // Act
            final LoginRequestValidationResult result = validator.validate(loginRequest);

            // Assert
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.loginId()).isEqualTo("user01");
            assertThat(result.password()).isEqualTo("1234");
            assertThat(result.strategy()).isEqualTo(roleStrategy);
        }

        @Test
        @DisplayName("매칭되는 RoleStrategy가 없을 때 strategy는 null이고 loginId/password도 null")
        void validate_noMatchingStrategy_returnsNullStrategyAndFields() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of(roleStrategy));
            final AccountUserLoginRequest loginRequest = AccountUserLoginRequest.of("user01", "1234");

            when(roleStrategy.supports(loginRequest.getClass())).thenReturn(false);
            when(beanValidator.validate(loginRequest)).thenReturn(Collections.emptySet());
            when(loginAccountValidator.supports(any())).thenReturn(true);

            // Act
            final LoginRequestValidationResult result = validator.validate(loginRequest);

            // Assert
            assertThat(result.hasErrors()).isFalse();
            assertThat(result.strategy()).isNull();
            assertThat(result.loginId()).isNull();
            assertThat(result.password()).isNull();
        }
    }

    // === validateDependencies ===

    @Nested
    @DisplayName("validateDependencies")
    class ValidateDependencies {

        @Test
        @DisplayName("loginRequestRoleStrategies가 null이면 IllegalStateException 발생")
        void validateDependencies_nullStrategies_throwsIllegalStateException() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(null);

            // Act & Assert
            assertThatThrownBy(validator::validateDependencies)
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("loginRequestRoleStrategies");
        }

        @Test
        @DisplayName("loginRequestRoleStrategies가 빈 리스트이면 IllegalStateException 발생")
        void validateDependencies_emptyStrategies_throwsIllegalStateException() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of());

            // Act & Assert
            assertThatThrownBy(validator::validateDependencies)
                    .isInstanceOf(GlobalException.class)
                    .hasMessageContaining("loginRequestRoleStrategies");
        }

        @Test
        @DisplayName("loginRequestRoleStrategies에 요소가 있으면 예외 없이 통과")
        void validateDependencies_nonEmptyStrategies_noException() {
            // Arrange
            final JsonBodyLoginRequestValidator validator = createValidator(List.of(roleStrategy));

            // Act & Assert (예외 없이 정상 수행)
            validator.validateDependencies();
        }
    }

    // === Helper Methods ===

    private JsonBodyLoginRequestValidator createValidator(final List<LoginRequestRoleStrategy> strategies) {
        return new JsonBodyLoginRequestValidator(beanValidator, loginAccountValidator, strategies);
    }

    @SuppressWarnings("unchecked")
    private <T> ConstraintViolation<T> createMockViolation(final String propertyPath, final String message) {
        final ConstraintViolation<T> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        final Path path = org.mockito.Mockito.mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
