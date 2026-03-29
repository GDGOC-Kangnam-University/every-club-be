package gdgoc.everyclub.clubrequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.Tag;
import gdgoc.everyclub.club.domain.TagNormalizer;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.club.repository.TagRepository;
import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestPayload;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
import gdgoc.everyclub.clubrequest.repository.ClubRequestRepository;
import gdgoc.everyclub.college.domain.Major;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubRequestService {

    private static final String DEPARTMENT_CLUB_CATEGORY = "학과동아리";

    private final ClubRequestRepository clubRequestRepository;
    private final ClubRepository clubRepository;
    private final CategoryRepository categoryRepository;
    private final MajorRepository majorRepository;
    private final TagRepository tagRepository;
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

    public List<ClubRequestAdminResponse> getClubRequests() {
        return clubRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(cr -> ClubRequestAdminResponse.from(cr, deserializePayload(cr.getPayload())))
                .toList();
    }

    public ClubRequestAdminResponse getClubRequest(UUID publicId) {
        ClubRequest clubRequest = clubRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
        return ClubRequestAdminResponse.from(clubRequest, deserializePayload(clubRequest.getPayload()));
    }

    @Transactional
    public ClubRegistrationResponse approveClubRequest(UUID publicId, Long adminUserId) {
        ClubRequest clubRequest = clubRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        if (clubRequest.getStatus() != RequestStatus.PENDING) {
            throw new LogicException(BusinessErrorCode.ALREADY_PROCESSED);
        }

        ClubRequestPayload payload = deserializePayload(clubRequest.getPayload());

        // 승인 시점에 slug 중복 재확인 (요청 이후 동일 slug의 동아리가 생성됐을 수 있음)
        if (clubRepository.existsBySlug(payload.slug())) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }

        Category category = categoryRepository.findById(payload.categoryId())
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        Major major = null;
        if (payload.majorId() != null) {
            major = majorRepository.findById(payload.majorId())
                    .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
        }

        Club club = Club.builder()
                .slug(payload.slug())
                .name(payload.name())
                .summary(payload.summary())
                .description(payload.description())
                .logoUrl(payload.logoUrl())
                .bannerUrl(payload.bannerUrl())
                .joinFormUrl(payload.joinFormUrl())
                .recruitingStatus(payload.recruitingStatus())
                .major(major)
                .activityCycle(payload.activityCycle())
                .hasFee(payload.hasFee())
                .isPublic(payload.isPublic())
                .category(category)
                .author(clubRequest.getRequestedBy())
                .build();

        clubRepository.save(club);

        if (payload.tags() != null && !payload.tags().isEmpty()) {
            resolveAndSetTags(club, payload.tags());
        }

        User admin = userService.getUserById(adminUserId);
        clubRequest.approve(admin);

        return ClubRegistrationResponse.from(clubRequest);
    }

    @Transactional
    public ClubRegistrationResponse rejectClubRequest(UUID publicId, Long adminUserId, ClubRequestRejectRequest request) {
        ClubRequest clubRequest = clubRequestRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        if (clubRequest.getStatus() != RequestStatus.PENDING) {
            throw new LogicException(BusinessErrorCode.ALREADY_PROCESSED);
        }

        User admin = userService.getUserById(adminUserId);
        clubRequest.reject(admin, request.adminMemo());

        return ClubRegistrationResponse.from(clubRequest);
    }

    private void resolveAndSetTags(Club club, List<String> rawTagNames) {
        List<String> normalized = rawTagNames.stream()
                .map(TagNormalizer::normalize)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        club.clearTags();
        for (String name : normalized) {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.of(name)));
            club.addTag(tag);
        }
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
                .tags(request.tags())
                .build();

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize club request payload", e);
        }
    }

    private ClubRequestPayload deserializePayload(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, ClubRequestPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize club request payload", e);
        }
    }
}