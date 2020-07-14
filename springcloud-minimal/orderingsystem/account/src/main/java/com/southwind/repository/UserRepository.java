package com.southwind.repository;

import com.southwind.entity.User;

public interface UserRepository {
    User login(String username, String password);
}
