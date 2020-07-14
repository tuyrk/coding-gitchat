package com.southwind.repository;

import com.southwind.entity.User;

import java.util.Collection;

public interface UserRepository {
    public Collection<User> findAll();

    public User findById(Long id);

    public void saveOrUpdate(User user);

    public void deleteById(Long id);
}
