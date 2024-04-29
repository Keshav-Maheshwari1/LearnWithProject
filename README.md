Dependencies needed

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt-jackson</artifactId>
	<version>0.11.5</version>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt-impl</artifactId>
	<version>0.11.5</version>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt-api</artifactId>
	<version>0.11.5</version>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-test</artifactId>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>io.projectreactor</groupId>
	<artifactId>reactor-test</artifactId>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.springframework.security</groupId>
	<artifactId>spring-security-test</artifactId>
	<scope>test</scope>
</dependency>

** Security Config Class**

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        http.
                cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(ex->{
                    ex.pathMatchers("/api/**").permitAll();
                }).csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex->ex.authenticationEntryPoint(new JwtAuthenticationPoint()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean

    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,PasswordEncoder passwordEncoder){
        UserDetailsRepositoryReactiveAuthenticationManager reactiveAuthenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        reactiveAuthenticationManager.setPasswordEncoder(passwordEncoder);
        return reactiveAuthenticationManager;

    }

}
******
Rest all is same as in non reactive programming
