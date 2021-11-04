package com.chat.webchat.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chat.webchat.mapper.TestMapper;
import com.chat.webchat.service.TestService;
import com.chat.webchat.vo.SignUpInfo;
import com.chat.webchat.vo.UserInfoVo;

@Service
public class TestServiceImpl implements TestService {
	private final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

	@Autowired
	TestMapper testMapper;
	

	@Override
	@Transactional("txManagerSvc")
	public String getDatabaseVersion() {
		String databaseVersion = testMapper.getDatabaseVersion();
		logger.debug("### DatabaseVersion : {}", databaseVersion);
		return databaseVersion;
	}

	@Override
	public UserInfoVo getUserInfo(String userId) {
		// TODO Auto-generated method stub
		
		UserInfoVo userInfo = testMapper.getUserInfo(userId);
		
		return userInfo;
	}
	
	@Transactional("txManagerSvc")
	@Override
	public void setUserInfo(SignUpInfo signUpInfo) {
		
		testMapper.setUserInfo(signUpInfo);
	}


}
