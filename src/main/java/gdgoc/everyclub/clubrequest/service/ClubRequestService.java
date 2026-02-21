package gdgoc.everyclub.clubrequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestPayload;
import gdgoc.everyclub.clubrequest.repository.ClubRequestRepository;
import gdgoc.everyclub.college.repository.MajorRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubRequestService {

    private static final String DEPARTMENT_CLUB_CATEGORY = "학과동아리";

    private final ClubRequestRepository clubRequestRepository;
    private final ClubRepository clubRepository;
    private final CategoryRepository categoryRepository;
    private final MajorRepository majorRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ClubRegistrationResponse createClubRequest(Long userId, ClubRegistrationRequest request) {
        if (clubRepository.existsBySlug(request.slug())) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        if (DEPARTMENT_CLUB_CATEGORY.equals(category.getName()) && request.majorId() == null) {
            throw new LogicException(ValidationErrorCode.MAJOR_REQUIRED_FOR_DEPARTMENT_CLUB);
        }

        if (request.majorId() != null) {
            majorRepository.findById(request.majorId())
                    .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
        }

        User requester = userService.getUserById(userId);

        String payloadJson = serializePayload(request);

        ClubRequest clubRequest = ClubRequest.builder()
                .requestedBy(requester)
                .payload(payloadJson)
                .build();

        ClubRequest saved = clubRequestRepository.save(clubRequest);
        return ClubRegistrationResponse.from(saved);
    }

    private String serializePayload(ClubRegistrationRequest request) {
        ClubRequestPayload payload = ClubRequestPayload.builder()
                .name(request.name())
                .categoryId(request.categoryId())
                .slug(request.slug())
                .summary(request.summary())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .bannerUrl(request.bannerUrl())
                .joinFormUrl(request.joinFormUrl())
                .recruitingStatus(request.recruitingStatus())
                .majorId(request.majorId())
                .activityCycle(request.activityCycle())
                .hasFee(request.hasFee())
                .isPublic(request.isPublic())
                .tagIds(request.tagIds())
                .build();

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize club request payload", e);
        }
    }
}
