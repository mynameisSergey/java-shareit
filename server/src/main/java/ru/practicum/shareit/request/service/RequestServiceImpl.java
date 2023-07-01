package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.RequestMapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public ItemRequestDto add(Long userId, ItemRequestDto itemRequestDto) {
        User user = UserMapper.toUser(userService.getUserById(userId));
        ItemRequest request = RequestMapping.toRequest(user, itemRequestDto);
        request.setRequester(user);
        return RequestMapping.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getUserRequests(Long userId) {
        UserMapper.toUser(userService.getUserById(userId));
        List<ItemRequest> itemRequestList = requestRepository.findAllByRequesterId(userId);
        return itemRequestList.stream()
                .map(RequestMapping::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        UserMapper.toUser(userService.getUserById(userId));
        List<ItemRequest> itemRequestList = requestRepository.findAllByRequester_IdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size));
        return itemRequestList.stream()
                .map(RequestMapping::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        Optional<ItemRequest> requestById = requestRepository.findById(requestId);

        if (requestById.isEmpty()) {
            log.debug("Запрос с id {} не был найден.", requestId);
            throw new NotFoundException(String.format("Запрос с id: %s " +
                    "не был найден.", requestId));
        }

        return RequestMapping.toRequestDto(requestById.get());
    }
}