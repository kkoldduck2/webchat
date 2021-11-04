package com.chat.webchat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chat.webchat.service.CustomUserDetailsService;

// 회원정보 : in memory -> 추후 DB 구축 

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	// creating and customizing filter chains
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http
			.csrf().disable()	// 기본값이 on인 csrf 취약점 보안을 해제한다. on으로 설정해도 되나, 설정할 경우 웹페이지에서 추가처리가 필요함
			.headers()
				.frameOptions().sameOrigin()	// SockJS는 기본적으로 HTML iframe 요소를 통한 전송을 허용하지 않도록 설정되는데 해당 내용을 해제한다.
			.and()
				.formLogin().loginPage("/login").defaultSuccessUrl("/chat/room",true)		// 권한없이 페이지 접근하면 로그인 페이지로 이동한다.
			.and()
				.authorizeRequests()		// authorization
					.antMatchers("/chat/**").hasRole("USER")	// chat으로 시작하는 리소스에 대한 접근 권한 설정
					.anyRequest().permitAll();	// 나머지 리소스에 대한 접근 설정
			
	        
	}
	
	/* 테스트를 위해 In-memory에 계정을 임의로 생성한다.
	 * 서비스에 사용시에는 DB 데이터를 이용하도록 수정이 필요하다. 
	 * */
	// authentication  : In-memory에서 DB 데이터 활용하도록 변경해야함. 
//	@Override
//	protected void configure(AuthenticationManagerBuilder builder) throws Exception{
//		// AuthenticationManagerBuilder : great for setting up in-memory or JDBC userDetails 
//		// or for adding a custom UserDetailsService. 
//		
//		builder.inMemoryAuthentication()
//				.withUser("sujin")
//				.password(passwordEncoder().encode("123"))
//				.roles("USER")
//			.and()
//				.withUser("december")
//				.password(passwordEncoder().encode("123"))
//				.roles("USER")
//			.and()
//				.withUser("guest")
//				.password(passwordEncoder().encode("123"))
//				.roles("GUEST");
//	}
	
//	@Override
//	public void configure(AuthenticationManagerBuilder builder) {
//	  builder.jdbcAuthentication()
//	  		 .dataSource(dataSource)
//	  		 .withUser("dave")
//	  		 .password("secret")
//	}
	// authenticationManagerBuilder : 이걸 이용해 (customizing한) authenticationManager를 생성한다.
	// 그렇게 생성된 authenticationManager는 auth가 유효한지 검사 후 Authentication을 반환한다. 
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth
		.userDetailsService(customUserDetailsService) // User : id, pw, authorities
		.passwordEncoder(passwordEncoder());
	}

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
