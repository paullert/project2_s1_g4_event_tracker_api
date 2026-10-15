package cst438.project2.backend.service;

import cst438.project2.backend.dto.InviteResponse;
import cst438.project2.backend.model.Event;
import cst438.project2.backend.model.Invite;
import cst438.project2.backend.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
}
