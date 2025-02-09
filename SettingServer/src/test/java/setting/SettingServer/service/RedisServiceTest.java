//package setting.SettingServer.service;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import setting.SettingServer.service.redis.RedisService;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class RedisServiceTest {
//
//    @Autowired
//    private RedisService redisService;
//
//    @Test
//    public void testRedisOperations() {
//        String key = "testKey";
//        String value = "testValue";
//
//        redisService.setValue(key, value);
//        assertEquals(value, redisService.getValue(key));
//
//        redisService.deleteValue(key);
//        assertNull(redisService.getValue(key));
//    }
//}