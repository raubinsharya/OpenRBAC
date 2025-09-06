package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PagedResponseMapper;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.UserSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public PagedResponse<UserDTO> getAllUsers(String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 0));
        return PagedResponseMapper.fromPage(
                userRepository.findAll(
                        UserSpecification.hasStatus(status), pageable),
                UserDTO::from);
    }
    public UserDTO getUserById(Long id) {
       return userRepository.findById(id).map(UserDTO::from).orElse(null);
    }
}