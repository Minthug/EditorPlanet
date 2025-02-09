package setting.SettingServer.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import setting.SettingServer.dto.MemberDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, MemberDto> redisTemplate;

//    public void save(String key, String value) {
//        redisTemplate.opsForValue().set(key, value);
//    }
//
//    public void setValue(String key, String value) {
//        redisTemplate.opsForValue().set(key, value);
//    }

    public MemberDto getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
