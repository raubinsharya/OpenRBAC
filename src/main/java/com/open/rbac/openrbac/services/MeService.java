package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.MeDTO;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeService {
    private final UserRepository userRepository;

    public MeDTO getUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return MeDTO.from(user);
    }
}
