package com.southwind.repository;

import com.southwind.entity.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll(int index, int limit);

    int count();

    void save(User user);

    void deleteById(long id);
}
