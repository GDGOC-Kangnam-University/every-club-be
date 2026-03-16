package gdgoc.everyclub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpResponse {
    private String message;
    private boolean success;
}