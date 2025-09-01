package ntou.cse.feelink_backend.chatroom.controller;

import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.chatroom.service.UserService;
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
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User user) {
        user.setUserId(userId);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody User user) {
        try {
            Optional<User> existingUser = userService.getUserById(user.getUserId());

            User syncedUser;
            if (existingUser.isPresent()) {
                User existing = existingUser.get();
                existing.setEmail(user.getEmail());
                existing.setNickname(user.getNickname());
                existing.setAINickname(user.getAINickname());
                syncedUser = userService.updateUser(existing);
            } else {
                syncedUser = userService.createUser(user);
            }

            return ResponseEntity.ok(syncedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}