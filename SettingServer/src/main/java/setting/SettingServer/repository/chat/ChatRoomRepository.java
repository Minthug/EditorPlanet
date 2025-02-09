package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.chat.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    Page<ChatRoom> findByChatRoomMembersId(String userId, Pageable pageable);

    @Query("select DISTINCT cr.id FROM  ChatRoom  cr " +
    "JOIN cr.chatRoomMembers m1 " +
    "JOIN cr.chatRoomMembers m2 " +
    "WHERE m1.id = :userId1 AND m2.id = :userId2")
    String findSharedChatRoom(@Param("userId2") String guestId, @Param("userId1") String roomMakerId);

}
    /*
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriberService redisSubscriber;

    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, MemberDto> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    /**
     * 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
     */
    /*
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    *//**
     * 채팅방 입장 : redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.
     *//*
    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }*/
