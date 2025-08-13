package com.example.ordersystem.common.config;

import com.example.ordersystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    // Qualifier : 같은 Bean객체가 여러 개 있을 경우, Bean객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")   // 리턴형이 같기 때문에 Qualifier로 구분
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);

        return (new LettuceConnectionFactory(configuration));
    }

    @Bean
    @Qualifier("stockInventory")
    // 개수 관리 redis
    public RedisConnectionFactory stockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);

        return (new LettuceConnectionFactory(configuration));
    }

    @Bean
    @Qualifier("rtInventory")   // rtInventory 템플릿
    // Bean들끼리 서로 의존성을 주입받을 때 메소드 파라미터로도 주입 가능
    // @Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory : 싱글톤 객체를 주입 받겠다
    // 모든 template 중에 무조건 redisTemplate 라는 메소드명이 반드시 1개는 있어야 함
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {  // <> 안에 key,value의 타입, <String, Object>로 설정한다면 객체를 받고 Json으로 형변환도 가능
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory); // RedisTemplate이 Redis와 통신할 수 있도록 실제 연결(Connection)을 설정

        return (redisTemplate);
    }

    @Bean
    @Qualifier("stockInventory")    // stockInventory 템플릿
    public RedisTemplate<String, String> stockTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory) {  // <> 안에 key,value의 타입, <String, Object>로 설정한다면 객체를 받고 Json으로 형변환도 가능
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory); // RedisTemplate이 Redis와 통신할 수 있도록 실제 연결(Connection)을 설정

        return (redisTemplate);
    }

    // redis pub/sub을 위한 연결객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);

        // redis pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음
        return (new LettuceConnectionFactory(configuration));
    }

    // ssePubSub 템플릿 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {  // <> 안에 key,value의 타입, <String, Object>로 설정한다면 객체를 받고 Json으로 형변환도 가능
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory); // RedisTemplate이 Redis와 통신할 수 있도록 실제 연결(Connection)을 설정

        return (redisTemplate);
    }

    // redis 리스너 객체
    @Bean
    @Qualifier("ssePubsub")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        // 만약 아래와 같이 여러 채널을 구독해야 하는 경우, 여러 개의 PatternTopic을 add하거나, 별도의 Bean 객체 생성
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel2"));

        return container;   // Bean이 바라보다가 이 채널에 들어온 메시지를 messageListenerAdapter()에 전달
    }

    // redis의 채널에서 수신된 메시지를 처리하는 Bean 객체
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        // 채널로부터 수신되는 message를 SseAlarmService의 onMessage메서드로 설정
        // 즉, 메시지가 수신되면 onMessage메서드가 호출
        return new MessageListenerAdapter(sseAlarmService, "onMessage");

    }
}
