package com.project.backend.controllers;

import com.project.backend.dto.RegisterDto;
import com.project.backend.dto.VerifyDto;
import com.project.backend.entities.RequestRefreshToken;
import com.project.backend.models.JwtRequest;
import com.project.backend.models.JwtResponse;
import com.project.backend.models.ResponseData;
import com.project.backend.security.JwtHelper;
import com.project.backend.services.MemberService;
import com.project.backend.services.impl.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MemberController {
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private MemberService memberService;
    @PostMapping("/register")
    public Mono<ResponseData> register(@RequestBody RegisterDto registerDto){
        return this.memberService.register(registerDto);
    }
    @PostMapping("/verify")
    public Mono<String> verify(@RequestBody VerifyDto verifyDto){
        return this.memberService.verifyAccount(verifyDto.getEmail(), verifyDto.getOtp());
    }
    @PostMapping("/regenerate")
    public Mono<String> regenerate(@RequestBody VerifyDto verifyDto){
        return this.memberService.regenerateOtp(verifyDto.getEmail());
    }
    @PostMapping("/log")
    public Mono<JwtResponse> login(@RequestBody JwtRequest request){
        return this.memberService.login(request);
    }
    @PostMapping("/refresh")
    public Mono<JwtResponse> refreshJwtToken(@RequestBody RequestRefreshToken refreshToken ){
        return refreshTokenService.verifyRefreshToken(refreshToken.getRefreshToken())
                .flatMap(refreshToken1 -> {
                    String email = refreshToken1.getEmail();
                    List<String> roles = new ArrayList<>();
                    Map<String,Object> rolesClaim = new HashMap<>();
                    String token = this.jwtHelper.generateToken(rolesClaim,email);

                    return Mono.just(new JwtResponse(email,token,refreshToken1.getToken()));
                })
                .switchIfEmpty(
                        Mono.just(
                                new JwtResponse("jwt Not generated","Expired refresh token","Not Found"
                                )
                        )
                );
    }
    @GetMapping
    public String hello(){
        return "Hello";
    }
}
