package com.project.backend.services.impl;

import com.project.backend.entities.RefreshToken;
import com.project.backend.repositories.MemberRepository;
import com.project.backend.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 60*60*1000;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    MemberRepository memberRepository;

    public Mono<RefreshToken> createRefreshToken(String email){
        return memberRepository.findByEmail(email)
                .flatMap(member -> {
                    RefreshToken refreshToken = member.getRefreshToken();
                    if(refreshToken == null) {
                        refreshToken = RefreshToken.builder()
                                .token(UUID.randomUUID().toString())
                                .expiry(Instant.now().plusMillis(JWT_REFRESH_TOKEN_VALIDITY))
                                .email(member.getEmail())
                                .build();
                    }else{
                       refreshToken.setExpiry(Instant.now().plusMillis(JWT_REFRESH_TOKEN_VALIDITY));
                    }
                    member.setRefreshToken(refreshToken);
                    return refreshTokenRepository.save(refreshToken);
                });
    }
    private Mono<Boolean> isRefreshTokenValid(String refreshToken){
        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> token.getExpiry().compareTo(Instant.now()) >= 0)
                .switchIfEmpty(Mono.just(false));
    }
    public Mono<RefreshToken> verifyRefreshToken(String refreshToken){
        return isRefreshTokenValid(refreshToken)
                .flatMap(isValid->{
                    if (isValid){
                        return refreshTokenRepository.findByToken(refreshToken);
                    }else{
                        System.out.println("Invalid Refresh Token");
                         return refreshTokenRepository.findByToken(refreshToken)
                                .flatMap(refreshTokenEntity->{
                                    refreshTokenRepository.delete(refreshTokenEntity);
                                    memberRepository.findByEmail(refreshTokenEntity.getEmail())
                                            .flatMap(member->memberRepository.delete(member));
                                    return Mono.empty();
                                });
                    }
                });
    }
}
