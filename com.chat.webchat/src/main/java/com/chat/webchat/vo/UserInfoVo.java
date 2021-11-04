package com.chat.webchat.vo;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoVo implements Serializable{

	private static final long serialVersionUID = -835836544233175645L;
	
	private String userId;
	private String password;
	private String authority;
	private String regDate;

}
