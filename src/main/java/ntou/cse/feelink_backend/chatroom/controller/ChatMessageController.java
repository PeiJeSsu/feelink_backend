package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.dto.ChatMessageDTO;
import ntou.cse.feelink_backend.chatroom.model.ChatMessage;
import ntou.cse.feelink_backend.chatroom.service.ChatMessageService;
import ntou.cse.feelink_backend.chatroom.service.EmotionAnalysisService;
import ntou.cse.feelink_backend.chatroom.service.GoogleNaturalLanguageService;

import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @Autowired
    private GoogleNaturalLanguageService googleNaturalLanguageService;

    @GetMapping
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages() {
        List<ChatMessage> messages = chatMessageService.getAllMessages();
        List<ChatMessageDTO> messageDTOs = MapperUtil.toChatMessageDTOList(messages);
        return ResponseEntity.ok(messageDTOs);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ChatMessageDTO> getMessageById(@PathVariable String messageId) {
        Optional<ChatMessage> message = chatMessageService.getMessageById(messageId);
        return message.map(msg -> ResponseEntity.ok(MapperUtil.toDTO(msg)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chatroom/{chatroomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByChatroomId(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> messageDTOs = MapperUtil.toChatMessageDTOList(messages);
        return ResponseEntity.ok(messageDTOs);
    }

    // 只取得使用者訊息
    @GetMapping("/chatroom/{chatroomId}/user")
    public ResponseEntity<List<ChatMessageDTO>> getUserMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userMessages = messages.stream()
                .filter(ChatMessage::getIsUser)
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userMessages);
    }

    // 只取得AI訊息
    @GetMapping("/chatroom/{chatroomId}/ai")
    public ResponseEntity<List<ChatMessageDTO>> getAIMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> aiMessages = messages.stream()
                .filter(msg -> !msg.getIsUser())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(aiMessages);
    }

    // 只取得一般文字訊息（非畫布資料）
    @GetMapping("/chatroom/{chatroomId}/text")
    public ResponseEntity<List<ChatMessageDTO>> getTextMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> textMessages = messages.stream()
                .filter(msg -> !msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(textMessages);
    }

    // 只取得畫布資料
    @GetMapping("/chatroom/{chatroomId}/drawing")
    public ResponseEntity<List<ChatMessageDTO>> getDrawingMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> drawingMessages = messages.stream()
                .filter(ChatMessage::getIsDrawingData)
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(drawingMessages);
    }

    // 取得使用者的畫布資料
    @GetMapping("/chatroom/{chatroomId}/user/drawing")
    public ResponseEntity<List<ChatMessageDTO>> getUserDrawingMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userDrawingMessages = messages.stream()
                .filter(msg -> msg.getIsUser() && msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDrawingMessages);
    }

    // 取得使用者的文字訊息
    @GetMapping("/chatroom/{chatroomId}/user/text")
    public ResponseEntity<List<ChatMessageDTO>> getUserTextMessages(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        List<ChatMessageDTO> userTextMessages = messages.stream()
                .filter(msg -> msg.getIsUser() && !msg.getIsDrawingData())
                .map(MapperUtil::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userTextMessages);
    }
    // 取得使用者文字訊息數量
    @GetMapping("/chatroom/{chatroomId}/user/text/count")
    public ResponseEntity<Map<String, Integer>> getUserTextMessageCount(@PathVariable String chatroomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByChatroomId(chatroomId);
        long count = messages.stream()
                .filter(msg -> msg.getIsUser() && !msg.getIsDrawingData())
                .count();

        Map<String, Integer> response = new HashMap<>();
        response.put("count", (int) count);
        return ResponseEntity.ok(response);
    }

    // 取得今天的使用者文字訊息數量
    @GetMapping("/chatroom/{chatroomId}/user/today/text/count")
    public ResponseEntity<Map<String, Integer>> getUserTextMessageCountToday(@PathVariable String chatroomId) {
        try {
            int count = chatMessageService.countUserTextMessagesToday(chatroomId);
            System.out.println(count);
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("取得今天使用者文字訊息計數失敗: " + e.getMessage());
            Map<String, Integer> errorResponse = new HashMap<>();
            errorResponse.put("count", 0);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<ChatMessageDTO> createMessage(@RequestBody ChatMessageDTO messageDTO) {
        ChatMessage message = MapperUtil.toEntity(messageDTO);
        ChatMessage createdMessage = chatMessageService.createMessage(message);
        ChatMessageDTO createdMessageDTO = MapperUtil.toDTO(createdMessage);
        return ResponseEntity.ok(createdMessageDTO);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ChatMessageDTO> updateMessage(@PathVariable String messageId, @RequestBody ChatMessageDTO messageDTO) {
        messageDTO.setMessageId(messageId);
        ChatMessage message = MapperUtil.toEntity(messageDTO);
        ChatMessage updatedMessage = chatMessageService.updateMessage(message);
        ChatMessageDTO updatedMessageDTO = MapperUtil.toDTO(updatedMessage);
        return ResponseEntity.ok(updatedMessageDTO);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        chatMessageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    // 刪除聊天室的所有訊息
    @DeleteMapping("/chatroom/{chatroomId}")
    public ResponseEntity<Void> deleteAllMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteAllMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除聊天室訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 刪除聊天室的使用者訊息
    @DeleteMapping("/chatroom/{chatroomId}/user")
    public ResponseEntity<Void> deleteUserMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteUserMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條使用者訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除使用者訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 刪除聊天室的AI訊息
    @DeleteMapping("/chatroom/{chatroomId}/ai")
    public ResponseEntity<Void> deleteAIMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteAIMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條AI訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除AI訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 只刪除聊天室的文字訊息
    @DeleteMapping("/chatroom/{chatroomId}/text")
    public ResponseEntity<Void> deleteTextMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteTextMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條文字訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除文字訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 只刪除聊天室的畫布訊息
    @DeleteMapping("/chatroom/{chatroomId}/drawing")
    public ResponseEntity<Void> deleteDrawingMessagesInChatroom(@PathVariable String chatroomId) {
        try {
            int deletedCount = chatMessageService.deleteDrawingMessagesByChatroomId(chatroomId);
            System.out.println("成功刪除聊天室 " + chatroomId + " 的 " + deletedCount + " 條畫布訊息");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("刪除畫布訊息時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/chatroom/{chatroomId}/emotion/today")
    public ResponseEntity<EmotionAnalysisService.EmotionScores> getTodayEmotionAnalysis(@PathVariable String chatroomId) {
        try {
            EmotionAnalysisService.EmotionScores emotionScores = emotionAnalysisService.analyzeTodayEmotions(chatroomId);
            return ResponseEntity.ok(emotionScores);
        } catch (Exception e) {
            System.err.println("當天情緒分析失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/chatroom/{chatroomId}/summary/today")
    public ResponseEntity<Map<String, String>> getTodayChatSummary(@PathVariable String chatroomId) {
        try {
            String summary = emotionAnalysisService.generateTodayChatSummary(chatroomId);

            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            response.put("date", emotionAnalysisService.getTaiwanTodayString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("取得當天聊天摘要失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/chatroom/{chatroomId}/demands/today")
    public ResponseEntity<String> getTodaySimpleDemand(@PathVariable String chatroomId) {
        try {
            String demand = emotionAnalysisService.analyzeTodayDemandSimple(chatroomId);
            return ResponseEntity.ok(demand);
        } catch (Exception e) {
            System.err.println("當天需求分析失敗: " + e.getMessage());
            return ResponseEntity.ok("需要被理解和傾聽");
        }
    }

    @GetMapping("/chatroom/{chatroomId}/score/today")
    public ResponseEntity<Map<String, Float>> getTodaySentiment(@PathVariable String chatroomId) {
        try {
            GoogleNaturalLanguageService.SimpleSentimentResult result = googleNaturalLanguageService.analyzeTodaySentiment(chatroomId);

            Map<String, Float> response = new HashMap<>();
            response.put("score", result.getScore());
            response.put("magnitude", result.getMagnitude());
            System.out.println("Score: " + result.getScore() + " Magnitude: " + result.getMagnitude());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("獲取當天情感分數失敗: " + e.getMessage());
            Map<String, Float> errorResponse = new HashMap<>();
            errorResponse.put("score", 0.0f);
            errorResponse.put("magnitude", 0.0f);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    @PostMapping("/chatroom/{chatroomId}/prequestion")
    public ResponseEntity<ChatMessageDTO> savePreQuestionForChatroom(
            @PathVariable String chatroomId,
            @RequestBody String preQuestion) {
        try {
            ChatMessage savedMessage = chatMessageService.savePreQuestionForNewChatroom(chatroomId, preQuestion);
            ChatMessageDTO savedMessageDTO = MapperUtil.toDTO(savedMessage);
            return ResponseEntity.ok(savedMessageDTO);
        } catch (Exception e) {
            System.err.println("儲存預設問題時發生錯誤: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}