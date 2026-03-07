package com.example.global.annotation;


import java.lang.annotation.*;

// 현재 로그인한 유저의 정보를 가져오기 위한 어노테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface CurrentAccount {
}
