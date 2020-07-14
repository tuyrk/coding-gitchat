package com.southwind.repository;

import com.southwind.entity.Admin;

public interface AdminRepository {
    Admin login(String username, String password);
}
