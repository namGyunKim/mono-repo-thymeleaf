package com.example.adminapi.domain.health.api;

import com.example.global.api.RestApiController;
import com.example.global.payload.response.RestApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("permitAll()")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthRestController {

    private final RestApiController restApiController;

    @GetMapping("/health")
    public ResponseEntity<RestApiResponse<String>> health() {
        return restApiController.ok("health");
    }
}
