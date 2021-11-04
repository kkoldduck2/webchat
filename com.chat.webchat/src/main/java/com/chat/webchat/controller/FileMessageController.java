package com.chat.webchat.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.chat.webchat.service.JwtTokenProvider;
import com.chat.webchat.vo.ChatMessage;
import com.chat.webchat.vo.ChatMessage.MessageType;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
@Controller
public class FileMessageController {
	private static Logger logger = LoggerFactory.getLogger(FileMessageController.class);
	private final RedisTemplate redisTemplate;
	private final ChannelTopic channelTopic;
	
	@Value("${server.file.location}")
	private String FILE_UPLOAD_PATH;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/file/upload") // submit 누르면 얘부터 실행 : handles a multi-part message file and gives it to the StorageService for saving
	@ResponseStatus(HttpStatus.CREATED)
	public void handleFileUpload(@RequestParam("file") MultipartFile file, 
			@RequestParam("roomNumber") String roomNumber,
			@RequestParam("token") String token) throws Exception {
		
		ChatMessage message = new ChatMessage();
		MessageType mt = MessageType.valueOf("file");
		
		// 서버에 저장
		String originalfileName = file.getOriginalFilename();
		String saveFileName = System.currentTimeMillis() + originalfileName.substring(originalfileName.lastIndexOf("."));
		message.setFileId(saveFileName);
		File dest = new File(FILE_UPLOAD_PATH + saveFileName);
		file.transferTo(dest);

		// TODO chatMessage 전송
		String nickname = jwtTokenProvider.getUserNameFromJwt(token);
		message.setSender(nickname);
		message.setType(mt);
		message.setRoomId(roomNumber);
		message.setMessage("파일 전송");	// 여기에 url 
		
		redisTemplate.convertAndSend(channelTopic.getTopic(), message);   // url 채팅창에 올리기 
	}
	// https://gofnrk.tistory.com/80
//	@GetMapping("/file/download/{fileId}")
//	public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") String fileId) throws IOException {
//		// 서버내의 디스크에 저장된 파일 경로
//		Path path = Paths.get("C:/toyServer/" + fileId);
//		String contentType = Files.probeContentType(path);	// 파일 데이터에서 직접 Content-type을 조사하여 response header에 세팅
//		
//		HttpHeaders headers = new HttpHeaders();
//		headers.add(HttpHeaders.CONTENT_TYPE, contentType);
//
//		Resource resource = new InputStreamResource(Files.newInputStream(path)); //  resource 생성 -> response의 body에 세팅하여 전송한다. 
//		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//	}
	
	@GetMapping("/file/download/{fileId}")
	public void fileDownload(@PathVariable("fileId") String fileId, HttpServletRequest request,HttpServletResponse response) {
		FileInputStream fis = null;
		OutputStream outs = null;
		try {
			
			fis = new FileInputStream(FILE_UPLOAD_PATH + fileId);
			outs = response.getOutputStream();
			
			byte[] buffer = new byte[4096];
			long totalLength = 0;
			int read =0;
			while((read = fis.read(buffer))!=-1) {
				totalLength +=read;
				outs.write(buffer,0,read);
			}
//			Path path = Paths.get("C:/toyServer/" + fileId);
			Path path = Paths.get(FILE_UPLOAD_PATH + fileId);
			String contentType = Files.probeContentType(path);	// 파일의 내용이 아니라 파일의 확장자를 이용하여 타입을 판단한다. 
			response.reset();
			response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachement: filename=" + fileId);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(totalLength));
		

		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			response.reset();
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);

			String errorMsg = "<html><head><script type='text/javascript'>alert('File not found...');</script></head><body>File not found...</body></html>\n";

			try {
				outs.write(errorMsg.getBytes());
			}
			catch (Exception ee) {
				logger.error(ee.getLocalizedMessage(), ee);
			}

		}
		finally {
			try {
				if(outs!=null)
					outs.close();
			}catch(IOException e) {
				
			}
			try {
				if(fis!=null)
					fis.close();
			}catch(IOException e) {
				
			}
		}
	}
}
