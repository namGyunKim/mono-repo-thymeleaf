package com.example.domain.aws.api;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.aws.payload.dto.AdminS3ImageDeleteCommand;
import com.example.domain.aws.payload.dto.AdminS3ImagesUploadCommand;
import com.example.domain.aws.payload.dto.AdminS3ImageUploadCommand;
import com.example.domain.aws.payload.dto.S3ImageUploadResult;
import com.example.domain.aws.payload.request.S3ImageDeleteRequest;
import com.example.domain.aws.payload.request.S3ImagesUploadRequest;
import com.example.domain.aws.payload.request.S3ImageUploadRequest;
import com.example.domain.aws.payload.response.S3ImagesUploadResponse;
import com.example.domain.aws.payload.response.S3ImageUploadResponse;
import com.example.domain.aws.service.command.AdminS3CommandService;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.version.ApiVersioning;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ConditionalOnProperty(name = "app.type", havingValue = "admin")
@RestController
@RequestMapping("/api/admin/aws/s3")
@RequiredArgsConstructor
@PreAuthorize("@memberGuard.hasAnyAdminRole()")
public class AdminS3ApiController {

    private final AdminS3CommandService adminS3CommandService;
    private final RestApiController restApiController;

    @PostMapping(value = "/images", params = "file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, version = ApiVersioning.V1)
    public ResponseEntity<RestApiResponse<S3ImageUploadResponse>> uploadImage(@CurrentAccount final CurrentAccountDTO currentAccount, @Valid @ModelAttribute("s3ImageUploadRequest") final S3ImageUploadRequest s3ImageUploadRequest) {
        final S3ImageUploadResult result = adminS3CommandService.uploadProfileImage(
                AdminS3ImageUploadCommand.of(currentAccount.id(), s3ImageUploadRequest.file())
        );

        final S3ImageUploadResponse response = S3ImageUploadResponse.from(result);

        return restApiController.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.memberImageId())
                        .toUri(),
                response
        );
    }

    @PostMapping(value = "/images", params = "files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, version = ApiVersioning.V1)
    public ResponseEntity<RestApiResponse<S3ImagesUploadResponse>> uploadImages(@CurrentAccount final CurrentAccountDTO currentAccount, @Valid @ModelAttribute("s3ImagesUploadRequest") final S3ImagesUploadRequest s3ImagesUploadRequest) {
        final S3ImagesUploadResponse response = S3ImagesUploadResponse.from(
                adminS3CommandService.uploadProfileImages(
                        AdminS3ImagesUploadCommand.of(currentAccount.id(), s3ImagesUploadRequest.files())
                )
        );

        return restApiController.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .build()
                        .toUri(),
                response
        );
    }

    @DeleteMapping(value = "/images", version = ApiVersioning.V1)
    public ResponseEntity<Void> deleteImages(@Valid @RequestBody final S3ImageDeleteRequest s3ImageDeleteRequest) {
        adminS3CommandService.deleteProfileImage(
                AdminS3ImageDeleteCommand.of(s3ImageDeleteRequest.memberImageId())
        );

        return restApiController.noContent();
    }
}
