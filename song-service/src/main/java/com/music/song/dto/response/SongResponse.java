package com.music.song.dto.response;

import com.music.song.entity.SongStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SongResponse {
    private Long id;
    private String title;
    private String audioFile;
    private String coverImage;
    private Long artistId;
    private String artistUsername;
    private Long albumId;
    private String albumName;
    private SongStatus status;
    private String rejectReason;
    private int playCount;
    private int likeCount;
    private boolean liked;
    private boolean premiumOnly;
    private LocalDateTime createdAt;
}
