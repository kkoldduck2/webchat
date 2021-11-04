package com.chat.webchat.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chat.webchat.service.TestService;
import com.chat.webchat.vo.SignUpInfo;
import com.chat.webchat.vo.UserInfoVo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class SignUpController {
	private final TestService testService;
	private final Logger logger = LoggerFactory.getLogger(SignUpController.class);
	
	@PostMapping("/register")
	public String Register(@RequestParam HashMap<Object, Object> params) {
		SignUpInfo signUpInfo = new SignUpInfo();
		String userId = (String) params.get("userId");
		String userPw = (String) params.get("userPw");
		
		signUpInfo.setUserId(userId);
		// password encoding
		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
		String encPw = bcryptPasswordEncoder.encode(userPw);
		
		signUpInfo.setUserPw(encPw);
		signUpInfo.setGrantType("ROLE_USER");
		testService.setUserInfo(signUpInfo);
		return "redirect:/login";
	}

// 아이디 존재 유무 검사 / 아이디 유효성 검사 
// return : boolean (해당 아이디가 이미 존재함/ 사용가능함 / 아이디에 스페이스가 있는지 검사 
	@PostMapping("/overlapId")
	@ResponseBody
	public boolean isIdOverlap(@RequestParam HashMap<Object, Object> params) {
		String userId = (String) params.get("user_id");
		// 아이디가 이미 존재하는지 검사
		UserInfoVo userInfo = testService.getUserInfo(userId);
		logger.debug("controller에서 userInfo의 형태 :: "+userInfo);
		if(userInfo == null) {
			return false;
		}else {
			return true;
		}
	}
}
