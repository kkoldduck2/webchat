package com.chat.webchat.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpInfo implements Serializable{
	
	private String userId;
	private String userPw;
	private String grantType;
	
}
