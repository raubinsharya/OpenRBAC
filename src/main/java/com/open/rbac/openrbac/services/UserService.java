package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.UserFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        @Retryable(retryFor = { SQLException.class, ConnectException.class,
                        TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
        public PagedResponse<UserDTO> getAllUsers(UserFilterRequest userFilterRequest, String realmIdentifier) {
                Specification<User> specification = Specification
                                .allOf(UserSpecification.hasStatus(userFilterRequest.getStatus())
                                                .and(UserSpecification.hasRealm(realmIdentifier))
                                                .and(UserSpecification.hasUserName(userFilterRequest.getUsername()))
                                                .and(UserSpecification.hasFirstName(userFilterRequest.getFirstName()))
                                                .and(UserSpecification.hasLastName(userFilterRequest.getLastName()))
                                                .and(UserSpecification.hasEmail(userFilterRequest.getEmail()))
                                                .and(UserSpecification.accountExpiryDateAfter(
                                                                userFilterRequest.getAccountExpiryDateAfter()))
                                                .and(UserSpecification.accountExpiryDateBefore(
                                                                userFilterRequest.getAccountExpiryDateBefore()))
                                                .and(UserSpecification.isAccountExpired(
                                                                userFilterRequest.getIsAccountExpired()))
                                                .and(BaseSpecification.withBaseFilters(userFilterRequest)));
                return PagedResponse.fromPage(userRepository.findAll(specification, userFilterRequest.toPageable()),
                                UserDTO::from);
        }

        @Retryable(retryFor = { SQLException.class, ConnectException.class,
                        TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
        @Cacheable(value = "users", key = "#realmIdentifier + '-' + #id")
        public UserDTO getUserById(Long id, String realmIdentifier) {
                Specification<User> specification = Specification
                                .allOf(UserSpecification.hasUserId(id))
                                .and(UserSpecification.hasRealm(realmIdentifier));
                return UserDTO.from(userRepository.findOne(specification)
                                .orElseThrow(() -> new EntityNotFoundException("User not found")));
        }
}