package setting.SettingServer.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import setting.SettingServer.dto.MemberProfileResponse;
import setting.SettingServer.entity.chat.ChatMessage;

@Service
@RequiredArgsConstructor
public class RedisPubSubService {

    private final RedisTemplate<String, MemberProfileResponse> redisTemplate;


    public void publish(ChannelTopic topic, ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
