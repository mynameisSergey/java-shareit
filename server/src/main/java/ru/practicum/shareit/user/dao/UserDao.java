package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserDao {
    User add(User user);

    User update(Long userId, User user);

    List<User> getAll();

    void delete(long userId);

    User getUserById(long userId);
}