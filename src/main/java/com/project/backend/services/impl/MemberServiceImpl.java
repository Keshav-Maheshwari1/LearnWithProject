package com.project.backend.services.impl;

import com.project.backend.dto.RegisterDto;
import com.project.backend.entities.Member;
import com.project.backend.models.JwtRequest;
import com.project.backend.models.JwtResponse;
import com.project.backend.models.ResponseData;
import com.project.backend.repositories.MemberRepository;
import com.project.backend.security.JwtHelper;
import com.project.backend.services.MemberService;
import com.project.backend.utils.EmailUtil;
import com.project.backend.utils.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private ReactiveAuthenticationManager reactiveAuthenticationManager;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private OtpUtil otpUtil;
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    MemberRepository memberRepository;
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public Mono<ResponseData> register(RegisterDto registerDTO) {

        return memberRepository.findByEmail(registerDTO.getEmail()).hasElement()
                .flatMap(isExists->{
                    if(isExists){
                        return Mono.just(new ResponseData(HttpStatus.CONFLICT,null,"Email Already Exists... Try Login"));
                    }else{
                        Member member = new Member();
                        member.setName(registerDTO.getName());
                        member.setEmail(registerDTO.getEmail());
                        member.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
                        String otp = otpUtil.generateOtp();
                        emailUtil.sendOtpEmail(registerDTO.getEmail(),otp);
                        member.setOtp(otp);
                        member.setOtpGeneratedTime(LocalDateTime.now());
                        member.setRefreshToken(null);
                        return memberRepository.save(member).map(member1 -> new ResponseData(HttpStatus.OK,member1,"Registered Successfully"));
                    }
                });
    }
    public Mono<String> verifyAccount(String email, String otp) {
        Member updatedMember = new Member();
        return memberRepository.findByEmail(email)
                .flatMap(member -> {
                    updatedMember.setId(member.getId());
                    updatedMember.setEmail(member.getEmail());
                    updatedMember.setName(member.getName());
                    updatedMember.setPassword(member.getPassword());
                    updatedMember.setOtp(member.getOtp());
                    updatedMember.setOtpGeneratedTime(updatedMember.getOtpGeneratedTime());
                    updatedMember.setRefreshToken(member.getRefreshToken());
                    if (member.getOtp().equals(otp) &&
                            Duration.between(member.getOtpGeneratedTime(), LocalDateTime.now())
                                    .getSeconds() < (4 * 60)) {
                        updatedMember.setActive(true);
                        return deleteMember(member.getEmail())
                                .then(memberRepository.save(updatedMember).then( Mono.just("Otp Verified You can Login")));
                    } else {
                        return Mono.just("Please regenerate OTP");
                    }
                });
    }
    public Mono<String> regenerateOtp(String email){
        return memberRepository.findByEmail(email)
                .flatMap(member -> {
                    String otp = otpUtil.generateOtp();
                    emailUtil.sendOtpEmail(email,otp);
                    member.setOtp(otp);
                    member.setOtpGeneratedTime(LocalDateTime.now());
                    return memberRepository.save(member).then(Mono.just("Email sent... Please Verify your account within 4 minutes"));
                })
                .switchIfEmpty(Mono.just("User not found with this email: " + email));
    }
    public Mono<JwtResponse> login(JwtRequest request){
        return this.doAuthenticate(request.getEmail(),request.getPassword())
                .flatMap(authentication ->{
                    List<String> roles = new ArrayList<>();
                    Map<String,Object> rolesClaim = new HashMap<>();

                    String token = this.jwtHelper.generateToken(rolesClaim,request.getEmail());
                    return refreshTokenService.createRefreshToken(request.getEmail())
                            .map(refreshToken -> new JwtResponse(refreshToken.getEmail(),token,refreshToken.getToken()));

                })
                .switchIfEmpty(Mono.just(
                        new JwtResponse("Token not generated","Refresh Token not generated","Invalid Authentication")
                ));
    }
//                                    List<String> roles = new ArrayList<>();
//                                    Map<String, Object> rolesClaim = new HashMap<>();
//                                    userDetails.getAuthorities().forEach(a -> roles.add(a.getAuthority()));
//                                    rolesClaim.put("roles", roles);

    public Mono<Authentication> doAuthenticate(String email, String password){
        try{
            UsernamePasswordAuthenticationToken authenticationToken = new
                    UsernamePasswordAuthenticationToken(email,password,List.of(new SimpleGrantedAuthority("USER")));
            // This block will be executed when authentication succeeds
            return reactiveAuthenticationManager.authenticate(authenticationToken);
        }catch(Exception e){
            System.out.println("BadCredentialsException: ");
            throw new BadCredentialsException("Invalid Username or Password!");
        }
    }

//    @Override
//    public Flux<Member> getAllMembers() {
//        return memberRepository.findAll();
//    }

//    @Override
//    public Mono<ResponseData> getMemberById(String id) {
//        return memberRepository.findById(id).map(member -> new ResponseData(HttpStatus.OK, member,"Member Found Successfully"))
//                .switchIfEmpty(Mono.just(new ResponseData(HttpStatus.NOT_FOUND,null,"Member Not Found")));
//    }

    @Override
    public Mono<String> deleteMember(String email) {
        return memberRepository.findByEmail(email)
                .flatMap(member -> memberRepository.delete(member)
                        .then(Mono.just("Deleted Successfully"))
                );
    }

    @Override
    public Mono<ResponseData> updatePassword(String email, String password) {
        Member updatedMember = new Member();
        return memberRepository.findByEmail(email)
                .flatMap(member ->{
                    updatedMember.setId(member.getId());
                    updatedMember.setEmail(member.getEmail());
                    updatedMember.setName(member.getName());
                    updatedMember.setPassword(password);
                    updatedMember.setOtp(member.getOtp());
                    updatedMember.setOtpGeneratedTime(updatedMember.getOtpGeneratedTime());
                    updatedMember.setActive(member.isActive());
                    return deleteMember(member.getEmail())
                            .then( memberRepository.save(updatedMember)
                                    .map(returnMember -> new ResponseData(HttpStatus.OK,returnMember,"Updated Success")));
                })
                .switchIfEmpty(Mono.just(new ResponseData(HttpStatus.NOT_FOUND,null,"Member Not Found")));
    }

    @Override
    public Mono<ResponseData> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).map(member -> new ResponseData(HttpStatus.OK, member,"Member Found Successfully"))
                .switchIfEmpty(Mono.just(new ResponseData(HttpStatus.NOT_FOUND,null,"Member Not Found")));
    }
}
