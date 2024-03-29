package com.chat.webchat.config;
// redis의 pub/sub 기능을 이용할 것이므로 MessageListener 설정을 추가

// 그리고 어플리케이션에서 redis 사용을 위해 redisTemplate 설정도 추가

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.chat.webchat.pubsub.RedisSubscriber;

// redis client bean 등록
@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	/**
	 * 단일 Topic 사용을 위한 Bean 설정
	 */
	@Bean
	public ChannelTopic channelTopic() {
		return new ChannelTopic("chatroom");
	}

	/**
	 * redis에 발행(publish)된 메시지 처리를 위한 리스너 설정
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter, ChannelTopic channelTopic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, channelTopic);
		return container;
	}

	/**
	 * 실제 메시지를 처리하는 subscriber 설정 추가
	 */
	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber, "sendMessage");
	}

	/**
	 * 어플리케이션에서 사용할 redisTemplate 설정
	 */
//	@Bean
//	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//		redisTemplate.setConnectionFactory(connectionFactory);
//		redisTemplate.setKeySerializer(new StringRedisSerializer());
//		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
//		return redisTemplate;
//	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

//	@Bean
//	RedisTemplate<?, ?> redisTemplate() {
//		RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
//		redisTemplate.setConnectionFactory(redisConnectionFactory());
//		return redisTemplate;
//	}
	
	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		return redisTemplate;
	}
}
