package gdgoc.everyclub.student;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentVerificationClientTest {
    @Test
    void check() {
    }

    @Test
    void getDomainPath() {
        String exampleEmail = "qwer.ty";
        String result = StudentVerificationClient.getDomainPath(exampleEmail);
        assertEquals("ty/qwer", result);
    }

    @Test
    void getDomainPath_Null() {
        assertThrows(NullPointerException.class, () -> StudentVerificationClient.getDomainPath(null));
    }


    @Test
    void getDomainPath_NoDot() {
        String domain = "nodot";
        String result = StudentVerificationClient.getDomainPath(domain);
        assertEquals("nodot", result);
    }

}