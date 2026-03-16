package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.SystemErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtpEmailServiceTest {

    @Mock
    private OtpEmailService mockOtpEmailService;

    @Test
    @DisplayName("OtpEmailService interface defines sendOtpEmail method")
    void interfaceDefinesSendOtpEmail() {
        // Verify the interface has the method
        assertThat(OtpEmailService.class)
                .hasDeclaredMethods("sendOtpEmail");
    }

    @Test
    @DisplayName("Mock implementation can be invoked")
    void mockImplementationCanBeInvoked() {
        // given
        String email = "test@example.com";
        String otpCode = "ABC123";

        // when
        mockOtpEmailService.sendOtpEmail(email, otpCode);

        // then
        verify(mockOtpEmailService).sendOtpEmail(email, otpCode);
    }

    @Test
    @DisplayName("NotImplementedOtpEmailService throws LogicException")
    void notImplementedServiceThrowsException() {
        // given
        NotImplementedOtpEmailService service = new NotImplementedOtpEmailService();

        // when/then
        assertThatThrownBy(() -> service.sendOtpEmail("test@example.com", "ABC123"))
                .isInstanceOf(LogicException.class)
                .satisfies(e -> assertThat(((LogicException) e).getErrorCode())
                        .isEqualTo(SystemErrorCode.NOT_IMPLEMENTED));
    }
}