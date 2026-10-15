package cst438.project2.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.repository.InviteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository){
        this.inviteRepository = inviteRepository;
    }

    @Transactional(readOnly = true)
    public List<InviteResponse> getInvitesForUser(Long userId){
        List<Invite> invites = inviteRepository.findByUserId(userId);
        return invites.stream().map(invite -> {
            Event event = invite.getEvent();

            return new InviteResponse(
                    invite.getId(), event.getId(),
                    event.getEventName(), event.getEventDescription(),
                    event.getStartsOn(), invite.getStatus(),
                    invite.getInvitedAt()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public long getAcceptedAttendeeCount(Long eventId) {
        return inviteRepository.countByEventIdAndStatus(eventId, "ACCEPTED");
    }

    @Transactional
    public Invite updateInvite(Long inviteId, Long userId, String status){
        Invite invite = inviteRepository.findById(inviteId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Invitation not found"
        ));

        //check to make sure invitations belonging to user
        if(!invite.getUserId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found");
        }

        if(!"ACCEPTED".equals(status) && !"DECLINED".equals(status)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation can only be ACCEPTED or DECLINED");
        }

        //can only answer pending invitations so once youve made your decision you can't change
        if(!"PENDING".equals(invite.getStatus())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation has already been answered");
        }

        //update invitation status
        invite.setStatus(status);
        invite.setRespondedAt(LocalDateTime.now());

        return inviteRepository.save(invite);
    }
}
