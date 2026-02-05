package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final UserService userService;

    @Transactional
    public Long createClub(ClubCreateRequest request) {
        if (request == null) {
            throw new NullPointerException("ClubCreateRequest cannot be null");
        }
        User author = userService.getUserById(request.authorId());
        Club club = new Club(request.title(), request.content(), author);
        clubRepository.save(club);
        return club.getId();
    }

    public Page<Club> getClubs(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return clubRepository.findAll(pageable);
    }

    public Club getClubById(Long id) {
        return clubRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void updateClub(Long id, ClubUpdateRequest request) {
        if (request == null) {
            throw new NullPointerException("ClubUpdateRequest cannot be null");
        }
        Club club = getClubById(id);
        club.update(request.title(), request.content());
    }

    @Transactional
    public void deleteClub(Long id) {
        Club club = getClubById(id);
        clubRepository.delete(club);
    }
}
