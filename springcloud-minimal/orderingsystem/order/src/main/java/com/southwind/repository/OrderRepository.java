package com.southwind.repository;

import com.southwind.entity.Order;

import java.util.List;

public interface OrderRepository {
    void save(Order order);

    List<Order> findAllByUid(long uid, int index, int limit);

    int countByUid(long uid);

    void deleteByMid(long mid);

    void deleteByUid(long uid);

    List<Order> findAllByState(int state, int index, int limit);

    int countByState(int state);

    void updateState(long id, long aid, int state);
}
