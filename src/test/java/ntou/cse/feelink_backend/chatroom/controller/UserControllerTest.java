package ntou.cse.feelink_backend.chatroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.chatroom.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setUserId("user123");
        testUser.setNickname("TestUser");
        testUser.setAINickname("AI TestUser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].nickname").value("TestUser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById("user123")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/user123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserById("user123");
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById("nonexistent");
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail("nonexistent@example.com");
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        User inputUser = new User();
        inputUser.setNickname("NewUser");
        inputUser.setAINickname("AI NewUser");
        inputUser.setEmail("new@example.com");

        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setUserId("user123");
        updatedUser.setNickname("UpdatedUser");
        updatedUser.setAINickname("AI UpdatedUser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.nickname").value("UpdatedUser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(userService).deleteUser("user123");

        // When & Then
        mockMvc.perform(delete("/api/users/user123"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser("user123");
    }

    @Test
    void syncUser_WhenUserExists_ShouldUpdateAndReturnUser() throws Exception {
        // Given
        User syncUser = new User();
        syncUser.setUserId("user123");
        syncUser.setNickname("SyncedUser");
        syncUser.setAINickname("AI SyncedUser");
        syncUser.setEmail("synced@example.com");

        User existingUser = new User();
        existingUser.setUserId("user123");
        existingUser.setNickname("OldUser");
        existingUser.setAINickname("AI OldUser");
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setUserId("user123");
        updatedUser.setNickname("SyncedUser");
        updatedUser.setAINickname("AI SyncedUser");
        updatedUser.setEmail("synced@example.com");

        when(userService.getUserById("user123")).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.nickname").value("SyncedUser"))
                .andExpect(jsonPath("$.email").value("synced@example.com"));

        verify(userService, times(1)).getUserById("user123");
        verify(userService, times(1)).updateUser(any(User.class));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void syncUser_WhenUserNotExists_ShouldCreateAndReturnUser() throws Exception {
        // Given
        User syncUser = new User();
        syncUser.setUserId("newUser123");
        syncUser.setNickname("NewSyncedUser");
        syncUser.setAINickname("AI NewSyncedUser");
        syncUser.setEmail("newsynced@example.com");

        when(userService.getUserById("newUser123")).thenReturn(Optional.empty());
        when(userService.createUser(any(User.class))).thenReturn(syncUser);

        // When & Then
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("newUser123"))
                .andExpect(jsonPath("$.nickname").value("NewSyncedUser"))
                .andExpect(jsonPath("$.email").value("newsynced@example.com"));

        verify(userService, times(1)).getUserById("newUser123");
        verify(userService, times(1)).createUser(any(User.class));
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    void syncUser_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        User syncUser = new User();
        syncUser.setUserId("user123");
        syncUser.setNickname("SyncedUser");
        syncUser.setAINickname("AI SyncedUser");
        syncUser.setEmail("synced@example.com");

        when(userService.getUserById("user123")).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncUser)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById("user123");
    }

    @Test
    void syncUser_WhenUpdateUserFails_ShouldReturnInternalServerError() throws Exception {
        // Given
        User syncUser = new User();
        syncUser.setUserId("user123");
        syncUser.setNickname("SyncedUser");
        syncUser.setAINickname("AI SyncedUser");
        syncUser.setEmail("synced@example.com");

        User existingUser = new User();
        existingUser.setUserId("user123");
        existingUser.setNickname("OldUser");
        existingUser.setAINickname("AI OldUser");
        existingUser.setEmail("old@example.com");

        when(userService.getUserById("user123")).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(any(User.class))).thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncUser)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById("user123");
        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void syncUser_WhenCreateUserFails_ShouldReturnInternalServerError() throws Exception {
        // Given
        User syncUser = new User();
        syncUser.setUserId("newUser123");
        syncUser.setNickname("NewSyncedUser");
        syncUser.setAINickname("AI NewSyncedUser");
        syncUser.setEmail("newsynced@example.com");

        when(userService.getUserById("newUser123")).thenReturn(Optional.empty());
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Create failed"));

        // When & Then
        mockMvc.perform(post("/api/users/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncUser)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById("newUser123");
        verify(userService, times(1)).createUser(any(User.class));
    }
}