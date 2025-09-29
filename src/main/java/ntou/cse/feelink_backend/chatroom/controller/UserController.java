package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.dto.UserDTO;
import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.chatroom.service.UserService;
import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = MapperUtil.toUserDTOList(users);
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            logger.error("獲取所有使用者時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            return user.map(u -> ResponseEntity.ok(MapperUtil.toDTO(u)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根據使用者ID獲取使用者時發生錯誤: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        try {
            Optional<User> user = userService.getUserByEmail(email);
            return user.map(u -> ResponseEntity.ok(MapperUtil.toDTO(u)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根據mail獲取使用者時發生錯誤: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        try {
            // 驗證必要欄位
            if (userDTO.getUserId() == null || userDTO.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            User user = MapperUtil.toEntity(userDTO);
            User createdUser = userService.createUser(user);
            UserDTO createdUserDTO = MapperUtil.toDTO(createdUser);

            logger.info("成功創建使用者: {}", createdUser.getUserId());
            return ResponseEntity.ok(createdUserDTO);
        } catch (Exception e) {
            logger.error("創建使用者時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO) {
        try {
            userDTO.setUserId(userId);
            User user = MapperUtil.toEntity(userDTO);
            User updatedUser = userService.updateUser(user);
            UserDTO updatedUserDTO = MapperUtil.toDTO(updatedUser);

            logger.info("成功更新使用者: {}", userId);
            return ResponseEntity.ok(updatedUserDTO);
        } catch (Exception e) {
            logger.error("更新使用者時發生錯誤: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            logger.info("成功刪除使用者: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("刪除使用者時發生錯誤: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@RequestBody UserDTO userDTO) {
        try {
            // 驗證必要欄位
            if (userDTO.getUserId() == null || userDTO.getUserId().trim().isEmpty()) {
                logger.warn("同步使用者請求缺少用戶ID");
                return ResponseEntity.badRequest().build();
            }

            if (userDTO.getNickname() == null || userDTO.getNickname().trim().isEmpty()) {
                logger.warn("同步使用者請求缺少暱稱");
                return ResponseEntity.badRequest().build();
            }

            Optional<User> existingUser = userService.getUserById(userDTO.getUserId());

            User syncedUser;
            if (existingUser.isPresent()) {
                // 更新現有用戶
                User existing = existingUser.get();
                existing.setEmail(userDTO.getEmail());
                existing.setNickname(userDTO.getNickname());
                existing.setAINickname(userDTO.getAINickname());
                existing.setPreferredAIPartner(userDTO.getPreferredAIPartner());
                syncedUser = userService.updateUser(existing);
                logger.info("成功更新現有使用者: {}", userDTO.getUserId());
            } else {
                // 創建新用戶
                User newUser = MapperUtil.toEntity(userDTO);
                syncedUser = userService.createUser(newUser);
                logger.info("成功創建新使用者: {}", userDTO.getUserId());
            }

            UserDTO syncedUserDTO = MapperUtil.toDTO(syncedUser);
            return ResponseEntity.ok(syncedUserDTO);

        } catch (IllegalArgumentException e) {
            logger.error("同步使用者時參數錯誤: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("同步使用者時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}