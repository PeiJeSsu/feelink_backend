package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.dto.UserDTO;
import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.chatroom.service.UserService;
import ntou.cse.feelink_backend.chatroom.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = MapperUtil.toUserDTOList(users);
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(u -> ResponseEntity.ok(MapperUtil.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(u -> ResponseEntity.ok(MapperUtil.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        User user = MapperUtil.toEntity(userDTO);
        User createdUser = userService.createUser(user);
        UserDTO createdUserDTO = MapperUtil.toDTO(createdUser);
        return ResponseEntity.ok(createdUserDTO);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO) {
        userDTO.setUserId(userId);
        User user = MapperUtil.toEntity(userDTO);
        User updatedUser = userService.updateUser(user);
        UserDTO updatedUserDTO = MapperUtil.toDTO(updatedUser);
        return ResponseEntity.ok(updatedUserDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@RequestBody UserDTO userDTO) {
        try {
            Optional<User> existingUser = userService.getUserById(userDTO.getUserId());

            User syncedUser;
            if (existingUser.isPresent()) {
                User existing = existingUser.get();
                existing.setEmail(userDTO.getEmail());
                existing.setNickname(userDTO.getNickname());
                existing.setAINickname(userDTO.getAINickname());
                syncedUser = userService.updateUser(existing);
            } else {
                User newUser = MapperUtil.toEntity(userDTO);
                syncedUser = userService.createUser(newUser);
            }

            UserDTO syncedUserDTO = MapperUtil.toDTO(syncedUser);
            return ResponseEntity.ok(syncedUserDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}