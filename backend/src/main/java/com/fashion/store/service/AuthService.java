package com.fashion.store.service;

import com.fashion.store.dto.AuthDTO.*;
import com.fashion.store.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfile getCurrentUser(String email);
}
