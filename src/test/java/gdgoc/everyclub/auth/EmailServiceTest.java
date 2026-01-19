package gdgoc.everyclub.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    private final EmailService emailService = new EmailService();

    @Test
    @DisplayName("학교 이메일(@knu.ac.kr)이면 true를 반환한다")
    void isSchoolEmail_Success() {
        assertTrue(emailService.isSchoolEmail("test@knu.ac.kr"));
    }

    @Test
    @DisplayName("학교 이메일이 아니면 false를 반환한다")
    void isSchoolEmail_Fail() {
        assertFalse(emailService.isSchoolEmail("test@gmail.com"));
    }

    @Test
    @DisplayName("이메일이 null이면 NullPointerException이 발생한다")
    void isSchoolEmail_Null() {
        assertThrows(NullPointerException.class, () -> emailService.isSchoolEmail(null));
    }
}
