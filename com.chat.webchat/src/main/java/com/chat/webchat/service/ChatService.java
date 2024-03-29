package com.chat.webchat.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.chat.webchat.repository.ChatRoomRepository;
import com.chat.webchat.vo.ChatMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatService {
	private final ChannelTopic channelTopic;
	private final RedisTemplate redisTemplate;
	private final ChatRoomRepository chatRoomRepository;
	
	// destination 정보에서 roomId 추출
	public String getRoomId(String destination) {
		int lastIndex = destination.lastIndexOf('/');
		if(lastIndex!=-1) {
			return destination.substring(lastIndex + 1);
		}else {
			return "";
		}
	}
	
	// 채팅방에 메시지 발송
	public void sendChatMessage(ChatMessage chatMessage) {
		chatMessage.setUserCount(chatRoomRepository.getUserCount(chatMessage.getRoomId()));
		if(ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
			chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
		}else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())){
			chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
		}
		redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
	}
}
