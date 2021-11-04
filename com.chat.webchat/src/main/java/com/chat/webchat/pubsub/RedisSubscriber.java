package com.chat.webchat.pubsub;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.chat.webchat.vo.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Redis 구독 서비스 구현
/* Redis에 메시지 발행이 될 때까지 대기하였다가 메시지가 발행되면 해당 메시지를 읽어 처리하는 리스너입니다.
 * */
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {
	private final ObjectMapper objectMapper;
	private final SimpMessageSendingOperations messagingTemplate;
	
	 /**
	  * Redis에서 메시지가 발행(publish)되면 대기하고 있던 Redis Subscriber가 해당 메시지를 받아 처리한다.
	  */
	 public void sendMessage(String publishMessage) {
	     try {
	         // ChatMessage 객채로 맵핑
	         ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
	         // 채팅방을 구독한 클라이언트에게 메시지 발송
	         messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessage.getRoomId(), chatMessage);
	     } catch (Exception e) {
	         log.error("Exception {}", e);
	     }
	 }
}


