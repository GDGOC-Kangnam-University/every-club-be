package gdgoc.everyclub.clubrequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubRequestRejectRequest(
        @NotBlank(message = "거절 사유는 필수입니다.")
        @Size(max = 500, message = "거절 사유는 500자 이내로 입력해주세요.")
        String adminMemo
) {
}