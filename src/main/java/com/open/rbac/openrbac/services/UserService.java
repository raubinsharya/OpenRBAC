package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.RequestParams.UserFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.UserSpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getAllUsers(UserFilterRequest userFilterRequest) {
        Specification<User> specification = Specification.allOf(UserSpecification.hasStatus(userFilterRequest.getStatus())
                .and(UserSpecification.hasUserName(userFilterRequest.getUsername()))
                .and(UserSpecification.hasFirstName(userFilterRequest.getFirstName()))
                .and(UserSpecification.hasLastName(userFilterRequest.getLastName()))
                .and(UserSpecification.hasEmail(userFilterRequest.getEmail()))
                .and(UserSpecification.createdBefore(userFilterRequest.getCreatedBefore()))
                .and(UserSpecification.createdAfter(userFilterRequest.getCreatedAfter()))
                .and(UserSpecification.updatedBefore(userFilterRequest.getUpdatedBefore()))
                .and(UserSpecification.updatedAfter(userFilterRequest.getUpdatedAfter()))
        );
        return PagedResponse.fromPage(userRepository.findAll(specification, userFilterRequest.toPageable()), UserDTO::from);
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id).map(UserDTO::from).orElse(null);
    }
}