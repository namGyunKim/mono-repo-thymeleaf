package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberUploadDirect;

import java.util.List;

public record MemberImagesStorageDeleteCommand(
        Long memberId,
        List<String> fileNames,
        MemberUploadDirect uploadDirect
) {

    public MemberImagesStorageDeleteCommand {
        if (fileNames != null) {
            fileNames = List.copyOf(fileNames);
        }
    }

    public static MemberImagesStorageDeleteCommand of(final Long memberId, final List<String> fileNames, final MemberUploadDirect uploadDirect) {
        return new MemberImagesStorageDeleteCommand(memberId, fileNames, uploadDirect);
    }
}
