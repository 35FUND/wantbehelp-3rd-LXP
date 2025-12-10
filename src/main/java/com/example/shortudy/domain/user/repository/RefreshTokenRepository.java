package com.example.shortudy.domain.user.repository;

import com.example.shortudy.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    Optional<RefreshToken> findByUserEmail(String userEmail);
    
    void deleteByUserEmail(String userEmail);

    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
