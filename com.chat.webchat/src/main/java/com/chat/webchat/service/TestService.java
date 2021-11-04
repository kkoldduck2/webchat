package com.chat.webchat.service;

import com.chat.webchat.vo.SignUpInfo;
import com.chat.webchat.vo.UserInfoVo;

public interface TestService {
	String getDatabaseVersion();
	UserInfoVo getUserInfo(String userId);
	void setUserInfo(SignUpInfo signUpInfo);
	
}
