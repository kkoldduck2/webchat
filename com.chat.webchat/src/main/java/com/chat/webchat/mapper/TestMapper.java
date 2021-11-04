package com.chat.webchat.mapper;

import com.chat.webchat.vo.SignUpInfo;
import com.chat.webchat.vo.UserInfoVo;

public interface TestMapper {

	/**
	 * @return String
	 * 
	 * DB 버전 정보 조회
	 */	
	String getDatabaseVersion();
	/**
	 * @param userId
	 * @return UserInfo
	 * 
	 * userId로 유저정보 조회
	 */
	UserInfoVo getUserInfo(String userId);
	
	void setUserInfo(SignUpInfo signUpInfo);

}
