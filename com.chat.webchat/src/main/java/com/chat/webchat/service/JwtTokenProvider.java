package com.chat.webchat.service;


import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
/* Jwt Token을 이용하여 websocket 통신 보안 강화
 * 기존에는 stomp header에 아무런 정보를 세팅하지 않았지만,
 * 아래 작업을 통해 jwt token을 세팅하여 검증된 token을 보낸 클라이언트만 통신이 가능하도록 설정하였다.
 * */
/* JwtTokenProvider : jwt를 생성하고, 검증하는 기능을 제공하는 컴포넌트
 * 로그인한 회원의 id 정보로 Jwt 토큰을 만들도록 메서드를 구현.
 * 검증된 토큰으로 회원의 id를 알아낼 수있는 메서드 추가.
 * Jwt 토큰의 유효성을 검증할 수 있는 메서드 추가.
 * */
/* Jwt : header + payload + signature
 * - Header : 이 JWT가 어떤 방식으로, 어떤 알고리즘을 사용하여 토큰화 했는지 명시
 * - Payload : 토큰에 사용자가 담고자 하는 정보를 담는 곳
 * - Signature : 위 토큰이 유효한지 유효하지 않은지에 대한 정보를 가짐. 암호화에 사용되는 key 값은 서버에 저장해 놓는다. 
 * 				 그리고 발행된 JWT 값이 서버에 들어왔을 때, 두 값을 비교해서 올바른 JWT 토큰이 맞는지 확인한다. 
 * */
@Slf4j
@Component
public class JwtTokenProvider {
	
	@Value("${spring.jwt.secret}")
	private String secretKey;
	
	private long tokenValidMilisecond = 1000L * 60 * 60;  // 토큰 유효시간 : 1시간
	
	/* 이름으로(id로) Jwt Token을 생성한다. 
	 * */
	public String generateToken(String name) {
		Date now = new Date();
		
		return Jwts.builder()
				.setId(name)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime()+tokenValidMilisecond))
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}
	
	/* jwt Token을 복호화하여 이름을 얻는다. 
	 * */
	public String getUserNameFromJwt(String jwt) {
		return getClaims(jwt).getBody().getId();
	}
	
	/* Jwt Token의 유효성을 체크한다. 
	 * */
	public boolean validateToken(String jwt) {
		return this.getClaims(jwt) != null;
	}
	
	// 토큰에 담긴 payload 가져오기 
	private Jws<Claims> getClaims(String jwt){
		try {
			return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
		}catch(SignatureException ex) {
//			log.error("Invalid JWT signature");
			throw ex;
		}catch (MalformedJwtException ex) {
//            log.error("Invalid JWT token");
            throw ex;
        } catch (ExpiredJwtException ex) {
//            log.error("Expired JWT token");
            throw ex;
        } catch (UnsupportedJwtException ex) {
//            log.error("Unsupported JWT token");
            throw ex;
        } catch (IllegalArgumentException ex) {
//            log.error("JWT claims string is empty.");
            throw ex;
        }
	}
	
}
