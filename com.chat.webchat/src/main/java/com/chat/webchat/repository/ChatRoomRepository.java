package com.chat.webchat.repository;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chat.webchat.vo.Room;

import lombok.RequiredArgsConstructor;
/*
 *  채팅방 정보는 초기화 되지 않도록 생성 시 Redis Hash에 저장하도록 처리한다.
 *  채팅방 정보를 조회할 때는 Redis Hash에 저장된 데이터를 불러오도록 메서드 내용을 수정한다.
 *  채팅방 입장 시에는 채팅방 id로 Redis topic을 조회하여 pub/sub 메시지 리스너와 연동한다. 
 * 
 * */
@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
    // 채팅방(topic)에 발행되는 메시지를 처리할 Listner
//    private final RedisMessageListenerContainer redisMessageListener;
    // 구독 처리 서비스
//    private final RedisSubscriber redisSubscriber;
    
	// Redis ChacheKeys
	private static final String CHAT_ROOMS ="CHAT_ROOM";
	private static final String USER_COUNT = "USER_COUNT";
	private static final String ENTER_INFO = "ENTER_INFO";
	
	
//    private final RedisTemplate<String, Object> redisTemplate;
	@Resource(name = "redisTemplate")	// 빈의 이름(redisTemplate)을 이용해서 주입할 객체를 검색한다. 
    private HashOperations<String, String, Room> opsHashChatRoom;
	
	@Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
	
	@Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

//    @PostConstruct
//    private void init() {
//        opsHashChatRoom = redisTemplate.opsForHash();
//    }

	// 모든 채팅방 조회
    public List<Room> findAllRoom() {
//    	System.out.println("chatRoomRepo : findAllRoom");
        return opsHashChatRoom.values(CHAT_ROOMS);
    }
    
    // 특정 채팅방 조회
    public Room findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
    public Room createChatRoom(String name) {
        Room chatRoom = Room.create(name);
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }
    
    // 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장
    public void setUserEnterInfo(String sessionId, String roomId) {
    	hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }
    
    // 유저 세션으로 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId) {
    	return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }
    
    // 유저 세션정보와 맵핑된 채팅방 ID 삭제
    public void removeUserEnterInfo(String sessionId) {
    	hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }
    
    // 채팅방 유저수 조회
    public long getUserCount(String roomId) {
    	return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }
    
    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId) {
    	return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L);
    }
    
    // 채팅방에 입장한 유저수 -1
    public long minusUserCount(String roomId) {
    	return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).orElse(0L);
    }

}
