package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.UserSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getAllUsers(String status) {
        return userRepository.findAll(UserSpecification.hasStatus(status.toUpperCase())).stream().map(UserDTO::from).toList();
    }
}