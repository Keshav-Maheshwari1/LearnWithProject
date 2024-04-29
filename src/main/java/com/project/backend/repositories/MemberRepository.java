package com.project.backend.repositories;

import com.project.backend.entities.Member;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MemberRepository extends ReactiveMongoRepository<Member, String> {
    Mono<Member> findByEmail(String email);
}
