package gdgoc.everyclub.student;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentVerificationClientTest {

    @Test
    void check() {
    }

    @Test
    void getDomainPath() {
        String exampleEmail="qwer.ty";
        String result = StudentVerificationClient.getDomainPath(exampleEmail);
        assertEquals("ty/qwer", result);
    }
}