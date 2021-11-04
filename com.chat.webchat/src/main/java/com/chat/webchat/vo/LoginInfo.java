package com.chat.webchat.vo;

import lombok.Getter;
import lombok.Setter;
// 로그인 정보(id 및 jwt 토큰)을 전달할 DTO를 생성한다. 
@Getter
@Setter
public class LoginInfo {
	private String name;
	private String token;
	
//	@Builder
//	public LoginInfo(String name, String token) {
//		this.name = name;
//		this.token = token;
//	}
}
