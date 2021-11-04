# webchat
서비스 주소 : 18.218.111.124:8080/chat/room

## 프로젝트 목표

- WebSocket을 이용한 메시지 시스템에 대한 이해
- Maria DB와 Redis 사용 → 두 DB의 usage 비교
- SpringSecurity + jwt 기반의 로그인 시스템 이해

## 기능 정의

- 로그인 : Id, pw 입력 후 DB에 저장된 데이터와 일치할 시 로그인
- 로그아웃 : 로그아웃 시 권한 반납 후 로그인 화면으로 redirect
- 회원가입 : 아이디 중복 검사, 비밀번호 확인 후 회원 정보 MariaDB에 저장
- 채팅방 입장
    - 입장 시 입장 메시지, 퇴장 시 퇴장 메시지 출력
    - 채팅방 인원 수 표시
- 채팅
- 파일 업로드 : 디렉토리에서 파일 찾아서 업로드
- 파일 다운로드 : 사진/파일 클릭 시 파일 다운로드

## 시스템 개념도 (Sequence diagram)

### 1) 회원가입/로그인

<img width="703" alt="image" src="https://user-images.githubusercontent.com/47748246/140065573-3fdf29ec-ac9b-4648-b423-3617b4df6c96.png">


- 사용자가 회원가입을 시도할때, 우선 중복되는 아이디가 없는지 검사하기 위해 다음과 같은 과정을 거쳐 회원관리 DB를 조회
- 이후 아이디가 중복되지 않고 비밀번호가 일치할 경우,  register api를 호출하여 DB에 저장하도록 함
- 로그인 : spring security

### 2) 채팅/ 채팅방

<img width="710" alt="image" src="https://user-images.githubusercontent.com/47748246/140065621-574eb5fd-d318-4909-813a-aaff6b2979ff.png">


- 로그인을 마치고 채팅방 리스트 화면에 입장하면
사용자는 채팅방을 생성하거나, 이미 존재하는 채팅방에 입장할 수 있다.
- 채팅방을 생성할 경우, 생성된 채팅방은 chatRoomRepository를 거쳐서 redisDB에 저장된다.
- 채팅방에 입장하게 되면 가장 처음으로 /websocket 서버와 handshake를 하고 (단일)TCP가 연결이 된다.
- 그리고나면 사용자 정보를 이용해서 Jwt 토큰을 생성해오고, 해당 토큰을 헤더에 추가하여 connect 요청한다.

## 메시징 시스템 주요 기술

### 1) WebSocket

- **개념**
    
<img width="390" alt="image" src="https://user-images.githubusercontent.com/47748246/140065668-2c0664d1-42e5-4333-b66a-bdcd9bcb118b.png">

    
    일반적으로 **HTTP 통신**은 단방향 통신으로 클라이언트에서 서버에 요청을 할 때마다
    TCP 커넥션을 연결한 후 응답이 오면  연결을 끊어 버린다.
    반면, **webSocket 프로토콜**은 최초 접속 시 클라이언트와 서버 사이에 handshake 가 일어나고 이후 하나의 TCP 커넥션을 이용해서 양방향 통신을 하게 된다.
    
    즉, 정리하면
    
    - WebSocket 프로토콜은 서버-클라이언트 간에 단일 TCP 커넥션을 이용해서 양방향 통신을 제공한다.
    → 따라서 단 방향 통신 기반의 http와 달리, 웹 페이지와 서버간 실시간 상호작용이 가능해진다.
    - 실시간성을 보장해야 하고 변경 사항의 빈도가 클 때 사용한다.
- **Handler를 이용하여 구현**
    - WebSocket 서버는 WebSocketHandler 인터페이스의 구현체를 통해서, 각 경로에 대한 핸들러를 구현한다.
    - Message 형식에 따라 TextWebSocketHandler나 BinaryWebSocketHandler를 확장해 구현한다.
    
    [Websocket Handler 구현 참고](https://www.notion.so/Websocket-Handler-39845377f589407da74a54c110e03a3b)
    

- **단점**
    - 메시지의 내용을 정의하지 않음 → 메시지 형식을 customizing 해야함 (Stomp는 그럴 필요 없음)
    - 각 connection 마다 WebsocketHandler를 구현해야함
        
        반면, STOMP는 @Controller 객체를 이용해서 조직적으로 관리할 수 있음
        즉, 메시지들은 STOMP의 Destination 헤더를 기반으로 @Controller 객체의@MethodMapping 메서드로 라우팅된다.
        
    - 구버전 익스플로러의 경우 websocket을 지원하지 않을 수 있다.

### 2) STOMP

- **개념**
    - WebSocket 프로토콜은 두 가지 유형(text, binary)의 메시지를 정의하고 있지만, 그 메시지의 내용까지는 정의하고 있지 않다.
    - STOMP는 WebSocket 위에서 동작하는 (Frame 기반의) sub-protocol로써, 클라이언트와 서버가 전송할 메시지의 유형, 형식, 내용들을 정의하는 매커니즘이다.
    - STOMP의 Frame은 아래 그림과 같은 형식을 가지고 있다.
    
<img width="670" alt="image" src="https://user-images.githubusercontent.com/47748246/140065748-d934a20f-a7f3-46ec-bb2c-22dc094dd477.png">
    
    - 위와 같은 과정을 통해서, STOMP는 Publish-Subscribe 매커니즘을 제공한다.
    - 기본적으로 PUB/SUB 구조로 되어있어 메시지를 발송하고, 메시지를 받아 처리하는 부분이 확실히 정해져 있기 때문에 개발하는 입장에서 명확하게 인지하고 개발할 수 있다.
    - 또한 Stomp를 이용하면 통신 메시지의 헤더에 값을 세팅할 수 있어 헤더 값을 기반으로 통신 시 인증처리를 구현하는 것도 가능하다.
    
- **pub/sub 컨셉을 채팅룹에 대입하면**
    - 채팅방을 생성한다. - pub/sub 구현을 위한 Topic이 하나 생성된다.
    - 채팅방에 입장한다. - Topic을 구독한다.
    - 채팅방에서 메시지를 보내고 받는다. - 해당 Topic으로 메시지를 발송하거나 (pub) 메시지를 받는다. (sub)

- **흐름도**
    
<img width="669" alt="image" src="https://user-images.githubusercontent.com/47748246/140065795-28fafc3f-fca0-45bc-a75b-253cfdfe33ef.png">

    

1. Websocket 커넥션으로부터 메시지를 전달받으면 STOMP frame으로 decode 하고, 추가 처리를 위해 ClientInboundChannel로 전송된다.
2. STOMP 메시지의 destination 헤더가 /app으로 시작한다면, @MessageMapping 정보와 매핑된 메서드를 호출한다.
3. 반면, 헤더가 /topic으로 시작한다면 메시지 브로커로 바로(직접) 라우팅된다.
4. brokerChannel : 메시지 브로커에게 메시지를 보낸다.
5. 이후, 메시지 브로커는 매칭된 구독자들에게  clientOutBoundChannel을 통해서 메시지를 브로드캐스팅한다.

- **장점**
    - 메시징 프로토콜을 만들고, 메시지 형식을 커스터마이징할 필요가 없다.
    - RabbitMQ, Redis와 같은 Message Broker를 이용해서, subscription을 관리하고 메시지를 브로드캐스팅할 수 있다.
    - WebSocket 기반으로 각 커넥션마다 WebSocketHandler를 구현하는 것보다, @Contoller 객체를 이용해서 조직적으로 관리할 수 있다.
        
        → 즉, 메시지들은 STOMP의 Destination 헤더를 기반으로 @Controller 객체의
        @MethodMapping 메서드로 라우팅된다.
        
    - STOMP의 Destination 및 Message Type을 기반으로 메시지를 보호하기 위해, Spring Security를 사용할 수 있다.
    

### 3) Redis PUB/SUB

- Redis as Message Broker
    
<img width="696" alt="image" src="https://user-images.githubusercontent.com/47748246/140065835-b61109b5-18aa-433b-80d3-67991b9a6b6b.png">

    

- **사용 이유**
    
    1) 채팅 서버 간 채팅방 공유
    
    - Stomp의 simple broker : 구독 대상인 채팅방(topic)이 생성된 서버 내에서만 메시지를 주고받는 것이 가능하다. 따라서 다른 서버로 접속한 클라이언트는 해당 채팅방이 보이지도 않고, 채팅방(topic) 구독도 불가능하다.
    - Redis의 PUB/SUB 시스템 : 구독 대상( 채팅방: topic)이 여러 서버에서 접근할 수 있도록 공통으로 사용할 수 있는 pub/sub 시스템을 구축
    - 서로 다른 서버에 접속해 있는 클라이언트가 채팅방을 통해 다른 서버의 클라이언트와 메시지를 주고 받을 수 있도록 한다.
    
    2) 저장소 역할
    
    - 서버를 재시작하더라도 채팅방이 계속 유지될 수 있도록 하기위해 Redis 저장소를 이용해서 저장

## Spring Security와 JWT를 이용한 사용자 인증/권한

### Spring Security

- 채팅과 관련된 웹 페이지 접근 권한은 Spring security를 통해 통제.
→ 로그인한 회원만 채팅 화면에 접근 가능하도록 처리.

<img width="601" alt="image" src="https://user-images.githubusercontent.com/47748246/140065908-53e1ba25-5b7d-4b09-8866-2eb1d056c806.png">


1. 사용자가 form을 통해 로그인 정보가 담긴 Request를 보냄
2. AuthenticationFilter: 사용자가 보낸 아이디와 패스워드를 인터셉트하여 유효성을 검증하고,
3. Authenticatoin 객체를 만들서 AuthenticationManager에 위임한다.
4. 실제 인증을 할 AuthenticationProvider에게 Authentication 객체를 전달한다.
5. AuthenticationProvider는 authenticaition()메서드에 파라미터로 입력한 로그인 정보를 Authentication 객체로 전달하고
6. UserDetailsService를 이용해 DB를 조회하여 사용자 정보를 검사한다.
7. 인증이 완료되면 사용자 정보를 가진 Authentication 객체를 SecurityContextHolder에 담은 이후 AuthenticationSuccessHandle을 실행한다.

### JWT (Json Web Token)

- WebSocket 연결 및 메시지 전송은 Jwt 토큰을 통해 통제.
→ Websocket 접속이나 메시지 전송 시엔 헤더에 유효한 Jwt Token을 보내야 하며, 유효하지 않은 token에 대해서는 요청 내용을 처리하지 않음.
    
<img width="681" alt="image" src="https://user-images.githubusercontent.com/47748246/140065959-6e71ccba-05c2-4158-8eda-ebf3203c6d02.png">

    
- 이후, 사용자가 STOMP를 이용하여 대화 메시지를 보낼 때 마다 헤더에 토큰을 append한다.
→ Websocket 통신 보안 강화****
