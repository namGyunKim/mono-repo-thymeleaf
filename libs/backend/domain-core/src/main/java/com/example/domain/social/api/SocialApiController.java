package com.example.domain.social.api;

import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.payload.request.GoogleRedirectRequest;
import com.example.domain.social.google.service.command.GoogleSocialLoginStartCommandService;
import com.example.domain.social.google.service.command.GoogleSocialRedirectCommandService;
import com.example.domain.social.google.service.query.GoogleSocialLoginStartQueryService;
import com.example.domain.social.google.validator.GoogleRedirectRequestValidator;
import com.example.domain.social.payload.response.SocialRedirectResponse;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.version.ApiVersioning;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

/**
 * 소셜 로그인 REST API 컨트롤러
 *
 * <p>
 * 외부 OAuth Provider 콜백 특성상 API-Version 헤더 전달이 불가능하여,
 * 이 컨트롤러의 콜백 엔드포인트는 헤더 버저닝 예외로 운영합니다.
 * README의 API-Version 정책에 따라 {@code /api/social/**}는 헤더 예외로 취급합니다.
 * </p>
 */
@PreAuthorize("permitAll()")
@ConditionalOnProperty(name = "app.type", havingValue = "user")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialApiController {

    private final GoogleSocialLoginStartCommandService googleSocialLoginStartCommandService;
    private final GoogleSocialLoginStartQueryService googleSocialLoginStartQueryService;
    private final GoogleSocialRedirectCommandService googleSocialRedirectCommandService;
    private final GoogleRedirectRequestValidator googleRedirectRequestValidator;
    private final RestApiController restApiController;

    @InitBinder("googleRedirectRequest")
    public void initGoogleRedirectBinder(final WebDataBinder binder) {
        binder.addValidators(googleRedirectRequestValidator);
    }

    @GetMapping(value = "/google/login", version = ApiVersioning.V1)
    public ResponseEntity<RestApiResponse<SocialRedirectResponse>> googleLogin() {
        googleSocialLoginStartCommandService.prepareLoginSession();
        final String redirectUrl = googleSocialLoginStartQueryService.getRedirectUrl();
        final SocialRedirectResponse response = SocialRedirectResponse.of(redirectUrl);
        return restApiController.ok(response);
    }

    /**
     * 외부 OAuth Provider 콜백은 API-Version 헤더 전달이 불가능합니다.
     * 따라서 이 엔드포인트는 URL 버저닝({@code /v1/...})을 사용하며, 헤더 버저닝 예외로 취급합니다.
     * 소셜 로그인 성공 시 세션이 설정된 상태로 홈페이지로 리다이렉트합니다.
     */
    @GetMapping(value = "/v1/google/redirect")
    public ResponseEntity<Void> googleRedirect(@Valid @ModelAttribute("googleRedirectRequest") final GoogleRedirectRequest googleRedirectRequest) {
        final GoogleSocialRedirectCommand command = GoogleSocialRedirectCommand.from(googleRedirectRequest);
        googleSocialRedirectCommandService.loginByRedirect(command);

        return ResponseEntity.status(302)
                .header("Location", "/")
                .build();
    }
}
