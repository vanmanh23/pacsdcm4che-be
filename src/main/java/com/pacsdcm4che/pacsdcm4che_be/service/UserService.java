package com.pacsdcm4che.pacsdcm4che_be.service;

import com.pacsdcm4che.pacsdcm4che_be.entity.UserEntity;
import com.pacsdcm4che.pacsdcm4che_be.exception.ResourceNotFoundException;
import com.pacsdcm4che.pacsdcm4che_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<UserEntity> createUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User already exists");
        }
        try {
            return ResponseEntity.ok(userRepository.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Error creating user", e);
        }
    }
    public ResponseEntity<UserEntity> getUserByUserName(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found");
        }
        return ResponseEntity.ok((UserEntity) userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        if (userRepository.count() == 0) {
            throw new ResourceNotFoundException("No users found");
        }
         return ResponseEntity.ok(userRepository.findAll());
    }
    public ResponseEntity<UserEntity> updateUser(Long id, UserEntity user) {
        UserEntity newUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());

        return ResponseEntity.ok(userRepository.save(newUser));
    }
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}
