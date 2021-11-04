package com.chat.webchat.vo;

import java.io.File;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
	public ChatMessage() {
		
	}
	
	@Builder
	public ChatMessage(MessageType type, String roomId, String sender, String message, long userCount) {
		this.type = type;
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.userCount = userCount;
	}
	
	public enum MessageType {
        ENTER, TALK, file, QUIT
    }
	
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지  -> 파일일 경우 url 반환
    private String fileId;
    private File file;
    private long userCount;
	
}
