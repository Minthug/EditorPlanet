package setting.SettingServer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import setting.SettingServer.dto.chat.ChatMessageDto;
import setting.SettingServer.entity.chat.ChatMessage;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final static String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final ChatMessageService chatMessageService;
    private final RabbitTemplate template;

    @MessageMapping("chat.message")
    public void sendMessage(ChatMessageDto message) {

        try {
            ChatMessage newChat = chatMessageService.createChatMessage(message);
            if (newChat != null) {
                template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + message.getRoomId(), newChat);
                log.info("Message sent to RabbitMQ: {}", newChat);
            } else {
                log.error("Failed to create chat message. User might not be in the chat room. User: {},  Room: {}",
                        message.getAuthorId(), message.getRoomId());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e);
        }
    }
}
