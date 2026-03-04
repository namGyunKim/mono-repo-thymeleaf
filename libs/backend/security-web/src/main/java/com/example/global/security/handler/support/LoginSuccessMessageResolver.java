package com.example.global.security.handler.support;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

/**
 * 로그인 성공 메시지를 결정하는 클래스
 */
@Component
public class LoginSuccessMessageResolver {

    public String resolve(final HttpServletRequest request) {
        return "로그인 성공";
    }
}
