package com.southwind.repository;

import com.southwind.entity.Menu;

import java.util.List;

public interface MenuRepository {
    List<Menu> findAll(int index,int limit);
    int count();
    void save(Menu menu);
    Menu findById(long id);
    void update(Menu menu);
    void deleteById(long id);
}
