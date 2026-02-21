package gdgoc.everyclub.clubrequest.controller;

import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.service.ClubRequestService;
import gdgoc.everyclub.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/club-requests")
@RequiredArgsConstructor
public class ClubRequestController {

    private final ClubRequestService clubRequestService;

    @PostMapping
    public ApiResponse<ClubRegistrationResponse> createClubRequest(
            @RequestBody @Valid ClubRegistrationRequest request) {
        // TODO: JWT에서 userId 추출 (SecurityContext 연동 후 교체)
        Long userId = 1L;
        ClubRegistrationResponse response = clubRequestService.createClubRequest(userId, request);
        return ApiResponse.success(response);
    }
}
