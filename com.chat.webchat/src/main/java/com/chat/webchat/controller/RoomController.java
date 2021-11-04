package com.chat.webchat.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.chat.webchat.repository.ChatRoomRepository;
import com.chat.webchat.service.JwtTokenProvider;
import com.chat.webchat.vo.LoginInfo;
import com.chat.webchat.vo.Room;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class RoomController {
	
	private final ChatRoomRepository chatRoomRepository;
	private final JwtTokenProvider jwtTokenProvider;
	
	/*방 페이지
	 *
	 * */

	@RequestMapping(value="/room")
	public String room() {
		return "room";
	}
	
	
	/*방 생성하기
	 * 
	 * */
	@PostMapping(value="/createRoom")
	@ResponseBody
	public  Room createRoom(@RequestParam HashMap<Object, Object> params) {
		String roomName = (String) params.get("roomName");
		return chatRoomRepository.createChatRoom(roomName);
	}

	/*방 정보 가져오기 
	 *  : 채팅방 리스트 조회시 userCount 정보를 세팅하도록 ChatRoomController 수정
	 * */
	@RequestMapping(value="/getRoomList")
	public @ResponseBody List<Room> getRoom(@RequestParam HashMap<Object, Object> params) {
		List<Room> chatRooms = chatRoomRepository.findAllRoom();
		chatRooms.stream().forEach(room -> room.setUserCount(chatRoomRepository.getUserCount(room.getRoomId())));
		return chatRooms;
	}
	
	/*채팅방
	 * 입장 후, 해당 client는 /topic/{roomNumber}를 구독할 것 
	 * */
	@RequestMapping(value="/moveChating")
	public String chating(Model model, @RequestParam HashMap<Object, Object> params) {
		model.addAttribute("roomName", params.get("roomName"));
		model.addAttribute("roomNumber", params.get("roomNumber"));
		return "chat";
	}

	/* 로그인 유저 정보 조회 api
	 * : 로그인한 회원의 Jwt 토큰 정보를 조회할 수 있도록 다음과 같이 추가
	 * */
	@RequestMapping(value="/user")
	@ResponseBody
	public LoginInfo getUserInfo() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();	
		String name = auth.getName();
		LoginInfo loginInfo = new LoginInfo();
		loginInfo.setName(name);
		String myToken = jwtTokenProvider.generateToken(name);
		loginInfo.setToken(myToken);	
		return loginInfo;
		
	}
}
