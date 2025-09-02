package com.pacsdcm4che.pacsdcm4che_be.service;

import com.pacsdcm4che.pacsdcm4che_be.dtos.CreateUserRequestDTO;
import com.pacsdcm4che.pacsdcm4che_be.entity.ERole;
import com.pacsdcm4che.pacsdcm4che_be.entity.Role;
import com.pacsdcm4che.pacsdcm4che_be.entity.UserEntity;
import com.pacsdcm4che.pacsdcm4che_be.exception.BusinessException;
import com.pacsdcm4che.pacsdcm4che_be.exception.ResourceNotFoundException;
import com.pacsdcm4che.pacsdcm4che_be.repository.RoleRepository;
import com.pacsdcm4che.pacsdcm4che_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public UserEntity createUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User already exists");
        }
        return userRepository.save(user);
    }

    public UserEntity getUserByUserName(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found");
        }
        return (UserEntity) userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    public List<UserEntity> getAllUsers() {
        if (userRepository.count() == 0) {
            throw new ResourceNotFoundException("No users found");
        }
        return userRepository.findAll();
    }

    public UserEntity updateUser(Long id, UserEntity user) {
        UserEntity newUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());
        return userRepository.save(newUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserEntity signUp(CreateUserRequestDTO createUserRequestDTO) {
        if (userRepository.findByUsername(createUserRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        UserEntity user = new UserEntity();
        user.setUsername(createUserRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(createUserRequestDTO.getPassword()));
        Set<String> strRoles = createUserRequestDTO.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public CreateUserRequestDTO getUserByUsername(String username) {
        UserEntity user = (UserEntity) userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO();
        createUserRequestDTO.setUsername(user.getUsername());
        createUserRequestDTO.setPassword(user.getPassword());
        Set<String> strRoles = new HashSet<>();
        for (Role role : user.getRoles()) {
            strRoles.add(role.getName().name());
        }
        createUserRequestDTO.setRole(strRoles);
        return createUserRequestDTO;
    }
}

