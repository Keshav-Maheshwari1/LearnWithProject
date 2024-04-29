package com.project.backend.services;

import com.project.backend.entities.Member;
import com.project.backend.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Qualifier
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private MemberRepository memberRepository;
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Mono<Member> byEmail = memberRepository.findByEmail(username);

        return memberRepository.findByEmail(username)
                .map(this::toUserDetails)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User Not Found With this email "+ username)));
    }
    private UserDetails toUserDetails(Member member){
        return User.withUsername(member.getEmail())
                .password(member.getPassword())
                .build();
    }

}
