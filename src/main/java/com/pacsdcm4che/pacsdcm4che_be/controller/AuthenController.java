package com.pacsdcm4che.pacsdcm4che_be.controller;

import com.pacsdcm4che.pacsdcm4che_be.dtos.CreateUserRequestDTO;
import com.pacsdcm4che.pacsdcm4che_be.dtos.AuthResponseDTO;
import com.pacsdcm4che.pacsdcm4che_be.dtos.LoginDTO;
import com.pacsdcm4che.pacsdcm4che_be.entity.UserEntity;
import com.pacsdcm4che.pacsdcm4che_be.security.JwtTokenProvider;
import com.pacsdcm4che.pacsdcm4che_be.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user")
public class AuthenController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

//    @PostMapping("/register")
//    public ResponseEntity<UserEntity> registerUser(@RequestBody @Valid UserEntity user) {
//        return userService.createUser(user);
//    }
    @GetMapping("/{username}")
    public ResponseEntity<UserEntity> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUserName(username));
    }
    @GetMapping("/all")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody @Valid UserEntity user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponseDTO(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody CreateUserRequestDTO signUpRequest) {
        return ResponseEntity.ok(userService.signUp(signUpRequest));
    }
    @GetMapping("/getusername/{token}")
    public String getUserInfo(@PathVariable String token) {
        return tokenProvider.getUsernameFromJWT(token);
    }
    @GetMapping("/getuser/{username}")
    public ResponseEntity<?> getRoleByUsername(@PathVariable String username) {
        CreateUserRequestDTO userFound =  userService.getUserByUsername(username);
        return ResponseEntity.ok(userFound);
    }
}
