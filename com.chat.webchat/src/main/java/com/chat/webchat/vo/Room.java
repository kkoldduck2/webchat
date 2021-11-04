package com.chat.webchat.vo;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.Setter;
// Redis에 저장되는 객체들은 Serialize가 가능해야 하므로 Serializable을 참조하도록 선언하고 serialVersionUID를 세팅해준다.
@Getter
@Setter
//@RedisHash("Room")		// 
public class Room implements Serializable{
	
	private static final long serialVersionUID =6494678977089006639L;
	
	private String roomId;
	private String roomName;
	private long userCount;	// 채팅방 인원수 
	
	public static Room create(String name) {
        Room chatRoom = new Room();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.roomName = name;
        return chatRoom;
    }
}
