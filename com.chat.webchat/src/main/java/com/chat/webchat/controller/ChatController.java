package com.chat.webchat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.chat.webchat.repository.ChatRoomRepository;
import com.chat.webchat.service.ChatService;
import com.chat.webchat.service.JwtTokenProvider;
import com.chat.webchat.vo.ChatMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatController {
	private static Logger logger = LoggerFactory.getLogger(ChatController.class);
	private final ChatRoomRepository chatRoomRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final ChatService chatService;
	static int fileUploadIdx = 0;
 /**
  * websocket "/topic/chat/message"로 들어오는 메시징을 처리한다.
  */
	 @MessageMapping("/chat/message")
	 public void message(ChatMessage message, @Header("token") String token) { 
		 String nickname = jwtTokenProvider.getUserNameFromJwt(token);
		 message.setSender(nickname);
		 chatService.sendChatMessage(message);
	 }
	 
}


