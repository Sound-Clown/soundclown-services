package com.music.auth.mapper;

import com.music.auth.dto.response.UserInfoDto;
import com.music.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserInfoDto toUserInfo(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
