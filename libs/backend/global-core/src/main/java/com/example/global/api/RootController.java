package com.example.global.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 루트("/") / favicon 요청 처리
 *
 * <ul>
 *   <li>"/" : Thymeleaf index 뷰 렌더링</li>
 *   <li>"/favicon.ico", "/favicon.svg" : 파비콘 파일이 없으면 204 응답</li>
 * </ul>
 */
@PreAuthorize("permitAll()")
@Controller
public class RootController {

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("message", "서버가 정상 작동 중입니다.");
        return "index";
    }

    @ResponseBody
    @RequestMapping({"/favicon.ico", "/favicon.svg"})
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
