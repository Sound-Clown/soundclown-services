package com.music.song.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewSongRequest {

    @NotNull(message = "VALIDATION_ERROR")
    private Boolean approved;

    // Required by convention only when rejecting; stored as-is.
    private String rejectReason;
}
