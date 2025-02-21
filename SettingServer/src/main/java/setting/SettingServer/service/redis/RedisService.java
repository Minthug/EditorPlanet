package setting.SettingServer.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import setting.SettingServer.dto.MemberProfileResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, MemberProfileResponse> redisTemplate;

//    public void save(String key, String value) {
//        redisTemplate.opsForValue().set(key, value);
//    }
//
//    public void setValue(String key, String value) {
//        redisTemplate.opsForValue().set(key, value);
//    }

    public MemberProfileResponse getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
