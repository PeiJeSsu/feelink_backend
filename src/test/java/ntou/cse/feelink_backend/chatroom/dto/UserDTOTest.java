package ntou.cse.feelink_backend.chatroom.dto;

import ntou.cse.feelink_backend.common.enums.AIPartner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserDTO
 */
class UserDTOTest {

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(userDTO);
        assertNull(userDTO.getUserId());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getNickname());
        assertNull(userDTO.getAINickname());
        assertNull(userDTO.getPreferredAIPartner());
    }

    @Test
    void testParameterizedConstructor() {
        UserDTO dto = new UserDTO("user123", "test@example.com", "測試用戶", "小助手", AIPartner.MUSE);

        assertEquals("user123", dto.getUserId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("測試用戶", dto.getNickname());
        assertEquals("小助手", dto.getAINickname());
        assertEquals(AIPartner.MUSE, dto.getPreferredAIPartner());
    }

    @Test
    void testSettersAndGetters() {
        userDTO.setUserId("user456");
        userDTO.setEmail("user@ntou.edu.tw");
        userDTO.setNickname("海大學生");
        userDTO.setAINickname("AI 夥伴");
        userDTO.setPreferredAIPartner(AIPartner.QIQI);

        assertEquals("user456", userDTO.getUserId());
        assertEquals("user@ntou.edu.tw", userDTO.getEmail());
        assertEquals("海大學生", userDTO.getNickname());
        assertEquals("AI 夥伴", userDTO.getAINickname());
        assertEquals(AIPartner.QIQI, userDTO.getPreferredAIPartner());
    }

    @Test
    void testNullValues() {
        userDTO.setUserId(null);
        userDTO.setEmail(null);
        userDTO.setNickname(null);
        userDTO.setAINickname(null);
        userDTO.setPreferredAIPartner(null);

        assertNull(userDTO.getUserId());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getNickname());
        assertNull(userDTO.getAINickname());
        assertNull(userDTO.getPreferredAIPartner());
    }

    @Test
    void testSetPreferredAIPartnerMuse() {
        userDTO.setPreferredAIPartner(AIPartner.MUSE);
        assertEquals(AIPartner.MUSE, userDTO.getPreferredAIPartner());
    }

    @Test
    void testSetPreferredAIPartnerQiQi() {
        userDTO.setPreferredAIPartner(AIPartner.QIQI);
        assertEquals(AIPartner.QIQI, userDTO.getPreferredAIPartner());
    }

    @Test
    void testSetPreferredAIPartnerNuanNuan() {
        userDTO.setPreferredAIPartner(AIPartner.NUANNUAN);
        assertEquals(AIPartner.NUANNUAN, userDTO.getPreferredAIPartner());
    }

    @Test
    void testGetPreferredAIPartnerDisplayNameWhenNotNull() {
        userDTO.setPreferredAIPartner(AIPartner.MUSE);
        assertEquals("謬思", userDTO.getPreferredAIPartnerDisplayName());
    }

    @Test
    void testGetPreferredAIPartnerDisplayNameWhenNull() {
        userDTO.setPreferredAIPartner(null);
        assertNull(userDTO.getPreferredAIPartnerDisplayName());
    }

    @Test
    void testEmptyStringValues() {
        userDTO.setUserId("");
        userDTO.setEmail("");
        userDTO.setNickname("");
        userDTO.setAINickname("");

        assertEquals("", userDTO.getUserId());
        assertEquals("", userDTO.getEmail());
        assertEquals("", userDTO.getNickname());
        assertEquals("", userDTO.getAINickname());
    }

    @Test
    void testEmailFormat() {
        userDTO.setEmail("student@mail.ntou.edu.tw");
        assertEquals("student@mail.ntou.edu.tw", userDTO.getEmail());
    }

    @Test
    void testConstructorWithNullValues() {
        UserDTO dto = new UserDTO(null, null, null, null, null);

        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getNickname());
        assertNull(dto.getAINickname());
        assertNull(dto.getPreferredAIPartner());
    }
}
