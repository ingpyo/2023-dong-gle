package org.donggle.backend.application.service.vendor.tistory.dto.request;

import lombok.Builder;

@Builder
public record TistoryPublishPropertyRequest(
        String access_token,
        String blogName,
        Long postId
) {
}