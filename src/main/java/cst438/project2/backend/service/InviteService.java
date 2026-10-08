package cst438.project2.backend.service;

import java.util.List;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.repository.InviteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
