package com.music.auth.repository;

import com.music.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Bulk DELETE executed immediately as SQL — must run before the new token is inserted.
    // A derived deleteByUserId queues removals that Hibernate flushes AFTER inserts, which
    // violates uk_prt_user_id when a token already exists for the user (forgot-password twice).
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PasswordResetToken t where t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
