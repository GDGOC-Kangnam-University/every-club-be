package gdgoc.everyclub.club.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitingStatus {
    OPEN("모집 중"),
    CLOSED("모집 종료");

    private final String description;
}
