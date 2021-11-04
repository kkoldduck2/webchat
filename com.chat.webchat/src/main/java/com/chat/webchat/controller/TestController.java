package com.chat.webchat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.webchat.service.TestService;

@RestController
public class TestController {
	private final Logger log = LoggerFactory.getLogger(TestController.class);

	@Autowired
	TestService testService;

	@GetMapping(value = "/test", name = "테스트용")
	public String test() {

		String rtn = testService.getDatabaseVersion();
		
		log.debug("### DatabaseVersion : {}", rtn);

		return rtn;

	}

}
