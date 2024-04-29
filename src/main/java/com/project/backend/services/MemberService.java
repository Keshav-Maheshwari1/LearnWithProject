package com.project.backend.services;

import com.project.backend.dto.RegisterDto;
import com.project.backend.entities.Member;
import com.project.backend.models.JwtRequest;
import com.project.backend.models.JwtResponse;
import com.project.backend.models.ResponseData;
import reactor.core.publisher.Mono;

public interface MemberService {
    Mono<ResponseData> register(RegisterDto registerDto);
    Mono<String> verifyAccount(String email, String otp);
    Mono<String> regenerateOtp(String email);
    Mono<JwtResponse> login(JwtRequest request);
    //    Flux<Member> getAllMembers();
//    Mono<ResponseData> getMemberById(String id);
    Mono<String> deleteMember(String email);
    Mono<ResponseData> updatePassword(String email,String password);
    Mono<ResponseData> findMemberByEmail(String email);
}
