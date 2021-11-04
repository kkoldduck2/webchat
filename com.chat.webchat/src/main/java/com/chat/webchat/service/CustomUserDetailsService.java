package com.chat.webchat.service;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chat.webchat.vo.UserInfoVo;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
	private final TestService testService;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		
		// userId를 이용해서 DB에서 유저 정보 가져오기 
		UserInfoVo userInfoVo = testService.getUserInfo(userId);
		
		// 없으면 exception 호출
		if(userInfoVo == null) {
			throw new UsernameNotFoundException(userId);
		}

		// 가져온 유저 정보에서 authority 꺼내 담기 
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(userInfoVo.getAuthority()));
		
		logger.debug("authorities = "+ authorities.toString());
		
		return new User(userId, userInfoVo.getPassword(), authorities);
	}
}
