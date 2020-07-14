package com.southwind.repository;

import com.southwind.entity.Type;

import java.util.List;

public interface TypeRepository {
    List<Type> findAll();
}
