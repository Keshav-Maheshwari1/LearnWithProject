package com.project.backend.repositories;

import com.project.backend.entities.Member;
import com.project.backend.entities.RefreshToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RefreshTokenRepository extends ReactiveMongoRepository<RefreshToken,String>{
    // FindByRefreshToken
    Mono<RefreshToken> findByToken(String refreshToken);

}
