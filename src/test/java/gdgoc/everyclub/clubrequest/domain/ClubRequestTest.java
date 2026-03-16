package gdgoc.everyclub.clubrequest.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClubRequestTest {

    private User requester;
    private User admin;

    @BeforeEach
    void setUp() {
        requester = new User("신청자", "requester@kangnam.ac.kr");
        admin = new User("관리자", "admin@kangnam.ac.kr");
    }

    // === approve() ===

    @Test
    @DisplayName("approve 호출 후 status가 APPROVED로 변경된다")
    void approve_changesStatusToApproved() {
        // given
        ClubRequest request = pendingRequest();

        // when
        request.approve(admin);

        // then
        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("approve 호출 후 reviewedBy가 admin으로 설정된다")
    void approve_setsReviewerToAdmin() {
        // given
        ClubRequest request = pendingRequest();

        // when
        request.approve(admin);

        // then
        assertThat(request.getReviewedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("approve 호출 후 reviewedAt이 null이 아니다")
    void approve_recordsReviewTime() {
        // given
        ClubRequest request = pendingRequest();

        // when
        request.approve(admin);

        // then
        assertThat(request.getReviewedAt()).isNotNull();
    }

    // === reject() ===

    @Test
    @DisplayName("reject 호출 후 status가 REJECTED로 변경된다")
    void reject_changesStatusToRejected() {
        // given
        ClubRequest request = pendingRequest();

        // when
        request.reject(admin, "규정에 부합하지 않습니다.");

        // then
        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    @DisplayName("reject 호출 후 전달한 memo가 adminMemo로 저장된다")
    void reject_savesAdminMemo() {
        // given
        ClubRequest request = pendingRequest();
        String memo = "중복 동아리 신청입니다.";

        // when
        request.reject(admin, memo);

        // then
        assertThat(request.getAdminMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("reject 호출 후 reviewedBy가 admin으로 설정된다")
    void reject_setsReviewerToAdmin() {
        // given
        ClubRequest request = pendingRequest();

        // when
        request.reject(admin, "사유");

        // then
        assertThat(request.getReviewedBy()).isEqualTo(admin);
    }

    @Test
    @DisplayName("신규 생성된 ClubRequest의 기본 status는 PENDING이다")
    void newRequest_defaultStatusIsPending() {
        // given & when
        ClubRequest request = pendingRequest();

        // then
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    // === 헬퍼 ===

    private ClubRequest pendingRequest() {
        return ClubRequest.builder()
                .requestedBy(requester)
                .payload("{}")
                .build();
    }
}