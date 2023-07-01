package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validation(user);
        var userObject = userRepository.save(user);
        return UserMapper.toUserDto(userObject);
    }

    @Transactional
    @Override
    public UserDto update(Long userId, UserDto userDto) {
        userNotExists(userId);
        User user = UserMapper.toUser(userDto);
        UserDto userFromStorage = getUserById(userId);
        if (Objects.isNull(user.getName())) {
            user.setName(userFromStorage.getName());
        }
        if (Objects.isNull(user.getEmail())) {
            user.setEmail(userFromStorage.getEmail());
        } else {
            String email = user.getEmail();
            boolean isEmailNotChange = userFromStorage.getEmail().equals(email);
            if (!isEmailNotChange) {
                validEmail(user);
            }
        }
        user.setId(userId);
        validation(user);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public List<UserDto> getAll() {
        return UserMapper.mapToUserDto(userRepository.findAll());
    }

    @Transactional
    public void delete(Long userId) {
        userNotExists(userId);
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserDto getUserById(Long userId) {
        userNotExists(userId);
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(this::validation);
        return UserMapper.toUserDto(user.get());
    }

    private void validation(User user) throws ValidationException {
        if (!StringUtils.hasText(user.getEmail())) {
            log.warn("Неправильно ввели почту");
            throw new ValidationException("Адрес электронной почты не может быть пустым.");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Неправильно ввели почту");
            throw new ValidationException("Адрес электронной почты не содержит @.");
        }
        if (user.getName().isBlank()) {
            log.warn("Неправильно ввели имя");
            throw new ValidationException("Имя пользователя не может быть пустым.");
        }
        if (user.getName().contains(" ")) {
            log.warn("Неправильно ввели имя");
            throw new ValidationException("Имя пользователя не может быть пустым");
        }
    }

    private void userNotExists(Long user) {
        userRepository.findById(user).orElseThrow(() -> {
            log.error("user service получает пользователя по ошибке: user с id {} не найден.", user);
            return new NotFoundException(String.format("Пользователь с id: %s не найден!", user));
        });
    }


    private void validEmail(User user) {
        String email = user.getEmail();
        List<User> userList = userRepository.findAll();
        for (User user1 : userList) {
            if (user1.getEmail().contains(email)) {
                log.error("user service получает email по ошибке: email {} уже существует.", email);
                throw new ValidationException("Адрес электронной почты уже существует.");
            }
        }
    }
}

