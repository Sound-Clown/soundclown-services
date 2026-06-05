package com.music.song.repository;

import com.music.song.dto.response.ArtistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// Aggregates artists from the APPROVED songs (GROUP BY artist_username). Uses JdbcTemplate
// because this is a reporting query with computed columns, not entity CRUD.
@Repository
@RequiredArgsConstructor
public class ArtistQueryRepository {

    private final JdbcTemplate jdbc;

    // Whitelist sortBy -> column (prevents SQL injection on the ORDER BY).
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "name", "artist_username",
            "songCount", "song_count",
            "albumCount", "album_count",
            "totalPlays", "total_plays");

    public List<ArtistResponse> list(int offset, int size, String sortBy, String sortDir) {
        String col = SORT_COLUMNS.getOrDefault(sortBy, "total_plays");
        String dir = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        String sql = "SELECT artist_username AS name, COUNT(*) AS song_count, "
                + "COUNT(DISTINCT album_id) AS album_count, COALESCE(SUM(play_count), 0) AS total_plays "
                + "FROM songs WHERE status = 'APPROVED' GROUP BY artist_username "
                + "ORDER BY " + col + " " + dir + ", artist_username ASC LIMIT ? OFFSET ?";
        return jdbc.query(sql, (rs, i) -> ArtistResponse.builder()
                .name(rs.getString("name"))
                .songCount(rs.getLong("song_count"))
                .albumCount(rs.getLong("album_count"))
                .totalPlays(rs.getLong("total_plays"))
                .build(), size, offset);
    }

    public long countArtists() {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT artist_username) FROM songs WHERE status = 'APPROVED'", Long.class);
        return c == null ? 0 : c;
    }
}
