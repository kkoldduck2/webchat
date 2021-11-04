package com.chat.webchat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
	@GetMapping("login")	// .loginPage("LOGIN_PAGE")에서 설정한 LoGIN_PAGE와 일치해야함 
	public String getLoginForm() {
		return "loginPage";
	}
}
