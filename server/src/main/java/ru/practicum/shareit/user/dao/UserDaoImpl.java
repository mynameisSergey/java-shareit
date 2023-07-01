package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong userId = new AtomicLong();

    @Override
    public User add(User user) {
        user.setId(userId.incrementAndGet());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь");
        return user;
    }

    @Override
    public User update(Long userId, User user) {
        users.put(userId, user);
        log.info("Пользователь {} обновлен", user.getName());
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }

    @Override
    public User getUserById(long userId) {
        return users.get(userId);
    }

    private boolean userNotExists(Long user) {
        return users.containsKey(user);
    }
}