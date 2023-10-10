package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
				.csrf((csrf) -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))) //csrf h2-console 해제
				.headers((headers) -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))); // x frame 방어 해제
		
		// 로그인 loginPage("로그인 url").defaultSuccessUrl("로그인 성공 시 url")
		http.formLogin((formLogin) -> formLogin.loginPage("/user/login").defaultSuccessUrl("/"))
			.logout((logout) -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
										.logoutSuccessUrl("/")
										.invalidateHttpSession(true));

		return http.build();
	}

	// PasswordEncoder는 BCryptPasswordEncoder의 인터페이스
	// BCryptPasswordEncoder 객체를 직접 new로 생성하는 방식보다는 PasswordEncoder 빈(bean)으로 등록해서
	// 사용
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// AuthenticationManager - 인증담당 
	// 사용자 인증시 UserSecurityService와 PasswordEncoder를 사용한다
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
