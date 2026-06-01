package com.music.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String artistUsername;

    @Field(type = FieldType.Text)
    private String albumName;

    @Field(type = FieldType.Keyword)
    private String coverImage;

    @Field(type = FieldType.Integer)
    private int playCount;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Long)
    private Long createdAt;   // epoch millis
}
