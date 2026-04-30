package gdgoc.everyclub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "내 프로필 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "닉네임", example = "동아리운영자")
        String nickname,

        @Schema(description = "학과명", example = "컴퓨터공학과")
        String department,

        @Schema(description = "학번", example = "202401234")
        @Size(max = 9, message = "학번은 9자 이하여야 합니다.")
        String studentId,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "자기소개", example = "학생 커뮤니티를 만드는 일에 관심이 있습니다.")
        String bio
) {
}
