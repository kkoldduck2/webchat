package com.chat.webchat.config.handler;

import java.security.Principal;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.chat.webchat.repository.ChatRoomRepository;
import com.chat.webchat.service.ChatService;
import com.chat.webchat.service.JwtTokenProvider;
import com.chat.webchat.vo.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/* 채팅방 입장시 이벤트 : StompCommand.SUBSCRIBE
 * 채팅방 퇴장시 이벤트 : StompCommand.DISCONNECT
 * */
@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor{
	
	private final JwtTokenProvider jwtTokenProvider;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatService chatService;
	
	// websocket을 통해 들어온 요청이 처리 되기전 실행된다. (handshake 실행 전)
	public Message<?> preSend(Message<?> message, MessageChannel channel){
		
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		
		if(StompCommand.CONNECT == accessor.getCommand()) {// websocket 연결 요청: 헤더의 jwt token 검증
			String jwtToken = accessor.getFirstNativeHeader("token");
			log.info("connect {}",jwtToken);
			jwtTokenProvider.validateToken(jwtToken);
//			jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("token"));
		}else if(StompCommand.SUBSCRIBE== accessor.getCommand()) {	// 채팅룸 구독 요청
			/* header 정보에 들어있는 simpDestination, simpSessionId를 가져온다.*/
			// header 정보에서 구독 destination 정보를 얻고, roomId를 추출한다.
			String roomId = chatService.getRoomId(Optional.ofNullable((String)message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));

			// 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다. (나중에 특정 세션이 어떤 채팅방에 들어가있는지 알기 위함
			String sessionId = (String)message.getHeaders().get("simpSessionId");
			chatRoomRepository.setUserEnterInfo(sessionId, roomId);
			
			// 채팅방의 인원수를 +1한다.
			chatRoomRepository.plusUserCount(roomId);
			// 클라이언트 입장 메시지를 채팅방에 발송한다. (redis publish)
			// String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
			// chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(name).build());
			// log.info("SUBSCRIBED {}, {}", name, roomId);
		}else if(StompCommand.DISCONNECT == accessor.getCommand()) {	// websocket 연결 종료
			// 연결이 종료된 클라이언트 sessionId로 채팅방 id를 얻는다.
			String sessionId = (String) message.getHeaders().get("simpSessionId");
			String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);
			
			// 채팅방의 인원수를 -1 한다
			chatRoomRepository.minusUserCount(roomId);
			
			// 클라이언트 퇴장 메시지를 채팅방에 발송한다. (redis publish)
			String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
			chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(name).build());
			
			// 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
			chatRoomRepository.removeUserEnterInfo(sessionId);
			log.info("DISCONNECTED {}, {}", sessionId, roomId);
		}
		return message;
		// 유효하지 않는 jwt 토큰이 세팅될 경우, websocket 연결을 하지 않고 예외 처리됩니다.
	}
}
