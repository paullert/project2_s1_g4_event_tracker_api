package cst438.project2.backend.service;

import cst438.project2.backend.model.InviteStatus;
import java.time.LocalDateTime;

import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.model.InviteId;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.EventRepository;
import cst438.project2.backend.repository.InviteRepository;

import cst438.project2.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


// TODO: review error handling with or else throw
@Service
public class InviteService {

  private static final String[] VALID_STATUS = {"ACCEPTED", "DECLINED", "PENDING"};

  private final InviteRepository inviteRepository;
  private final UserService userService; // TODO: prob way to avoid this
  private final UserRepository userRepository;
  private final EventRepository eventRepository;

  public InviteService(InviteRepository inviteRepository, UserService userService,
      UserRepository userRepository, EventRepository eventRepository) {
    this.inviteRepository = inviteRepository;
    this.userService = userService;
    this.userRepository = userRepository;
    this.eventRepository = eventRepository;
  }

  @Transactional(readOnly = true)
  public Page<InviteResponse> getInvitesForUser(Long userId, Pageable pageable) {
    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page must be greater than or equal to 0");
    }

    Page<Invite> invites = inviteRepository.findByUser_UserId(userId, pageable);

    return invites.map(InviteResponse::init);
  }

  // TODO: make it a proper json return
  @Transactional(readOnly = true)
  public long getAcceptedAttendeeCount(Long eventId) {
    return inviteRepository.countByEvent_IdAndStatus(eventId, "ACCEPTED");
  }

  // Updates a present invite
  // Finds invite based on body, must provide userId and eventId
  //  Todo: Review problem caused by or else through
  @Transactional
  public InviteResponse updateInvite(InviteRequest request) {
    Long userId = request.userId();
    Long eventId = request.eventId();
    if (userId == null || eventId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId or eventId");
    }
    Invite invite = findInvite(userId, eventId);
//        .orElseThrow(() -> new ResponseStatusException(
//            HttpStatus.NOT_FOUND, "Invitation not found"
//        ));

    String status = request.status();
    String message = request.message();

    //check to make sure invitations belonging to user
    if (!invite.getUser().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found");
    }

    if (message == null || status == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invitation message or status missing");
    }

    if (!InviteStatus.PENDING.equals(invite.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Invitation has already been answered");
    }

    // Checks if valid, throws err if not
    isValidStatus(status);

    //update invitation status
    invite.setStatus(InviteStatus.valueOf(status));
    invite.setRespondedAt(LocalDateTime.now());
    invite.setMessage(message);

    invite = inviteRepository.save(invite);
    return InviteResponse.init(invite);
  }


  // Gets a page of invites
  public Page<InviteResponse> getAllInvites(Pageable pageable) {
    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page must be greater than or equal to 0");
    }
    Page<Invite> invites = inviteRepository.findAll(pageable);
    return invites.map(InviteResponse::init);
  }

  public InviteResponse getInvite(Long userId, Long eventId) {
    Invite invite = findInvite(userId, eventId);
    return InviteResponse.init(invite);
  }


  // Gets a page of a user's invites using auth as a reference
  public Page<InviteResponse> getMyInvites(Authentication auth, Pageable pageable) {
    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page must be greater than or equal to 0");
    }

    User user = userService.findUserFromAuthentication(auth); // TODO: look into other solutions

    Page<Invite> invites = inviteRepository.findByUser_UserId(user.getUserId(), pageable);
    return invites.map(InviteResponse::init);
  }

  // Gets a specific user's invite based on eventId and their auth
  public InviteResponse getMyInvite(Authentication auth, Long eventId) {
    User user = userService.findUserFromAuthentication(auth); // TODO: look into other solutions
    Invite invite = findInvite(user.getUserId(), eventId);
    return InviteResponse.init(invite);
  }

  @Transactional
  public InviteResponse respondToMyInvite(Authentication auth, Long eventId,
      MyInviteRequest request) {
    User user = userService.findUserFromAuthentication(auth); // TODO: look into other solutions
    Invite invite = findInvite(user.getUserId(), eventId);

    if (!InviteStatus.PENDING.equals(invite.getStatus())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Invitation has already been answered");
    }

    String status = request.status();

    isValidStatus(status);
    invite.setStatus(InviteStatus.valueOf(status));
    invite.setRespondedAt(LocalDateTime.now());
    return InviteResponse.init(inviteRepository.save(invite));
  }

  // Gets all invites given out from an event
  public Page<InviteResponse> getEventInvites(Long eventId, Pageable pageable) {
    Page<Invite> invites = inviteRepository.findByEvent_Id(eventId, pageable);
    return invites.map(InviteResponse::init);
  }

  // Creates an invite tied to a user and event
  //   must provide userId and eventId
  public InviteResponse createInvite(InviteRequest request) {
    User user = userRepository.findById(request.userId()).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    );
    Event event = eventRepository.findById(request.eventId()).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")
    );

    InviteStatus status = request.status() == null ? InviteStatus.PENDING : InviteStatus.valueOf(request.status());
    Invite invite = new Invite(
        new InviteId(user.getUserId(), event.getId()),
        user,
        event,
        status,
        request.message(),
        LocalDateTime.now(),
        null
    );
    invite = inviteRepository.save(invite);
    return InviteResponse.init(invite);
  }

  // creates pending invites for multiple users
  @Transactional
  public List<InviteResponse> createEventInvites(
          Long eventId,
          List<Long> userIds
  ) {
    if (eventId == null) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "event id is required"
      );
    }

    if (userIds == null || userIds.isEmpty()) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "at least one user id is required"
      );
    }

    Event event = eventRepository.findById(eventId).orElseThrow(
            () -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "event not found"
            )
    );

    Set<Long> uniqueUserIds = new HashSet<>(userIds);

    if (uniqueUserIds.size() != userIds.size()) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "duplicate user ids were included"
      );
    }

    LocalDateTime invitedAt = LocalDateTime.now();

    List<Invite> invites = userIds.stream()
            .map(userId -> {
              User user = userRepository.findById(userId).orElseThrow(
                      () -> new ResponseStatusException(
                              HttpStatus.NOT_FOUND,
                              "user not found"
                      )
              );

              if (inviteRepository.existsByUser_UserIdAndEvent_Id(userId, eventId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "user already has an invitation for this event"
                );
              }

              return new Invite(
                      new InviteId(userId, eventId),
                      user,
                      event,
                      InviteStatus.PENDING,
                      null,
                      invitedAt,
                      null
              );
            })
            .toList();

    return inviteRepository.saveAll(invites)
            .stream()
            .map(InviteResponse::init)
            .toList();
  }

  // Partially Updates an invite
  //      Finds invite based on body, must provide userId and eventId
  // TODO: Second Pass this
  public InviteResponse patchInvite(InviteRequest request) {
    Long userId = request.userId();
    Long eventId = request.eventId();
    if (userId == null || eventId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Missing 1 or more required fields: userId, eventId");
    }

    Invite invite = inviteRepository.findByUser_UserIdAndEvent_Id(userId, eventId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Invitation not found"
        ));

    if (request.status() != null) {
      InviteStatus status = InviteStatus.valueOf(request.status());
      invite.setStatus(status);
      invite.setRespondedAt(LocalDateTime.now());
    }

    String message = request.message();
    if (message != null) {
      invite.setMessage(message);
    }



    if (message != null) { // If msg is present...
      invite.setMessage(message);
    }

    invite = inviteRepository.save(invite);
    return InviteResponse.init(invite);
  }

  // Checks if status matches one of our states, throw Err if not
  public static boolean isValidStatus(String status) {
    if (!"ACCEPTED".equals(status) && !"DECLINED".equals(status)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invitation can only be ACCEPTED or DECLINED");
    }

    return true;
  }

  private Invite findInvite(Long userId, Long eventId) {
    Invite invite = inviteRepository.findByUser_UserIdAndEvent_Id(userId, eventId)
        .orElse(null); // Todo: Handle Error Better
    if (invite == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found");
    }
    return invite;
  }
}
