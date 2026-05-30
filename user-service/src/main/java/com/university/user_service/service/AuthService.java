package com.university.user_service.service;

import com.university.user_service.dto.JwtResponse;
import com.university.user_service.dto.LoginRequest;
import com.university.user_service.dto.MessageResponse;
import com.university.user_service.dto.SignupRequest;

public interface AuthService {

    MessageResponse registerUser(SignupRequest signupRequest);

    JwtResponse authenticateUser(LoginRequest loginRequest);
}
