package com.university.user_service.service;

import com.university.user_service.dto.UserDTO;

public interface UserService {

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO updateUser(Long id, UserDTO dto);

    void deleteUser(Long id);
}
