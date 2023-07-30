package org.donggle.backend.domain.writing.content;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.donggle.backend.domain.writing.BlockType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageContent extends Content {
    private static final int DEFAULT_DEPTH = 0;

    @NotNull
    @Embedded
    private ImageUrl imageUrl;
    @NotNull
    @Embedded
    private ImageCaption imageCaption;

    public ImageContent(final BlockType blockType, final ImageUrl imageUrl, final ImageCaption imageCaption) {
        super(DEFAULT_DEPTH, blockType);
        this.imageUrl = imageUrl;
        this.imageCaption = imageCaption;
    }

    public String getImageUrlValue() {
        return imageUrl.getImageUrl();
    }

    public String getImageCaptionValue() {
        return imageCaption.getImageCaption();
    }
}