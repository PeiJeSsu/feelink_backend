package ntou.cse.feelink_backend.chatroom.service;

import ntou.cse.feelink_backend.chatroom.model.User;
import ntou.cse.feelink_backend.chatroom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("user123");
        testUser.setNickname("TestUser");
        testUser.setAINickname("AI TestUser");
        testUser.setEmail("test@example.com");

        anotherUser = new User();
        anotherUser.setUserId("user456");
        anotherUser.setNickname("AnotherUser");
        anotherUser.setAINickname("AI AnotherUser");
        anotherUser.setEmail("another@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).isNotNull();
        assertThat(actualUsers).hasSize(2);
        assertThat(actualUsers).containsExactlyInAnyOrder(testUser, anotherUser);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).isNotNull();
        assertThat(actualUsers).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> actualUser = userService.getUserById("user123");

        // Then
        assertThat(actualUser).isPresent();
        assertThat(actualUser.get()).isEqualTo(testUser);
        assertThat(actualUser.get().getUserId()).isEqualTo("user123");
        assertThat(actualUser.get().getNickname()).isEqualTo("TestUser");
        assertThat(actualUser.get().getAINickname()).isEqualTo("AI TestUser");
        assertThat(actualUser.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> actualUser = userService.getUserById("nonexistent");

        // Then
        assertThat(actualUser).isEmpty();
        verify(userRepository, times(1)).findById("nonexistent");
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> actualUser = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(actualUser).isPresent();
        assertThat(actualUser.get()).isEqualTo(testUser);
        assertThat(actualUser.get().getUserId()).isEqualTo("user123");
        assertThat(actualUser.get().getNickname()).isEqualTo("TestUser");
        assertThat(actualUser.get().getAINickname()).isEqualTo("AI TestUser");
        assertThat(actualUser.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> actualUser = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertThat(actualUser).isEmpty();
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void createUser_ShouldSaveAndReturnUser() {
        // Given
        User inputUser = new User();
        inputUser.setNickname("NewUser");
        inputUser.setAINickname("AI NewUser");
        inputUser.setEmail("new@example.com");

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User actualUser = userService.createUser(inputUser);

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser).isEqualTo(testUser);
        verify(userRepository, times(1)).save(inputUser);
    }

    @Test
    void createUser_WithCompleteUserData_ShouldSaveUser() {
        // Given
        User completeUser = new User();
        completeUser.setUserId("newUser123");
        completeUser.setNickname("CompleteUser");
        completeUser.setAINickname("AI CompleteUser");
        completeUser.setEmail("complete@example.com");

        when(userRepository.save(completeUser)).thenReturn(completeUser);

        // When
        User actualUser = userService.createUser(completeUser);

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getUserId()).isEqualTo("newUser123");
        assertThat(actualUser.getNickname()).isEqualTo("CompleteUser");
        assertThat(actualUser.getAINickname()).isEqualTo("AI CompleteUser");
        assertThat(actualUser.getEmail()).isEqualTo("complete@example.com");
        verify(userRepository, times(1)).save(completeUser);
    }

    @Test
    void updateUser_ShouldSaveAndReturnUpdatedUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setUserId("user123");
        updatedUser.setNickname("UpdatedUser");
        updatedUser.setAINickname("AI UpdatedUser");
        updatedUser.setEmail("updated@example.com");

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User actualUser = userService.updateUser(updatedUser);

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser).isEqualTo(updatedUser);
        assertThat(actualUser.getUserId()).isEqualTo("user123");
        assertThat(actualUser.getNickname()).isEqualTo("UpdatedUser");
        assertThat(actualUser.getAINickname()).isEqualTo("AI UpdatedUser");
        assertThat(actualUser.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void updateUser_WithPartialData_ShouldSaveUser() {
        // Given
        User partialUser = new User();
        partialUser.setUserId("user123");
        partialUser.setNickname("PartiallyUpdatedUser");
        // Email and AINickname are null

        when(userRepository.save(partialUser)).thenReturn(partialUser);

        // When
        User actualUser = userService.updateUser(partialUser);

        // Then
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getUserId()).isEqualTo("user123");
        assertThat(actualUser.getNickname()).isEqualTo("PartiallyUpdatedUser");
        assertThat(actualUser.getEmail()).isNull();
        assertThat(actualUser.getAINickname()).isNull();
        verify(userRepository, times(1)).save(partialUser);
    }

    @Test
    void deleteUser_ShouldCallRepositoryDeleteById() {
        // Given
        String userId = "user123";

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WithDifferentUserId_ShouldCallRepositoryDeleteById() {
        // Given
        String userId = "differentUser456";

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WithNullUserId_ShouldStillCallRepository() {
        // Given
        String userId = null;

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    // Integration-style tests to verify method interactions
    @Test
    void createUser_ThenGetById_ShouldReturnSameUser() {
        // Given
        User newUser = new User();
        newUser.setUserId("integration123");
        newUser.setNickname("IntegrationUser");
        newUser.setAINickname("AI IntegrationUser");
        newUser.setEmail("integration@example.com");

        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userRepository.findById("integration123")).thenReturn(Optional.of(newUser));

        // When
        User createdUser = userService.createUser(newUser);
        Optional<User> foundUser = userService.getUserById("integration123");

        // Then
        assertThat(createdUser).isEqualTo(newUser);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(newUser);
        verify(userRepository, times(1)).save(newUser);
        verify(userRepository, times(1)).findById("integration123");
    }

    @Test
    void createUser_ThenGetByEmail_ShouldReturnSameUser() {
        // Given
        User newUser = new User();
        newUser.setUserId("email123");
        newUser.setNickname("EmailUser");
        newUser.setAINickname("AI EmailUser");
        newUser.setEmail("emailtest@example.com");

        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userRepository.findByEmail("emailtest@example.com")).thenReturn(Optional.of(newUser));

        // When
        User createdUser = userService.createUser(newUser);
        Optional<User> foundUser = userService.getUserByEmail("emailtest@example.com");

        // Then
        assertThat(createdUser).isEqualTo(newUser);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(newUser);
        verify(userRepository, times(1)).save(newUser);
        verify(userRepository, times(1)).findByEmail("emailtest@example.com");
    }
}