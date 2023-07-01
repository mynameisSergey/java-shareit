package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDtoOut add(Long userId, BookingDto bookingDto) {
        User user = UserMapper.toUser(userService.getUserById(userId));
        Optional<Item> itemById = itemRepository.findById(bookingDto.getItemId());
        itemById.orElseThrow(() -> new NotFoundException(String.format("Вещь с id %s не найдена.", bookingDto.getItemId())));
        log.error("Вещь с id {} не найдена.", bookingDto.getItemId());
        Item item = itemById.get();
        bookingValidation(bookingDto, user, item);
        Booking booking = BookingMapper.toBooking(user, item, bookingDto);
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOut update(Long userId, Long bookingId, Boolean approved) {
        Booking booking = validateBookingDetails(userId, bookingId, 1);
        assert booking != null;
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoOut getBookingById(Long userId, Long bookingId) {
        Booking booking = validateBookingDetails(userId, bookingId, 2);
        assert booking != null;
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> getAll(Long bookerId, String state, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userService.getUserById(bookerId);
        switch (BookingState.valueOf(state)) {
            case ALL:
                return bookingRepository.findAllByBookerId(bookerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByBookerId(bookerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> getAllOwner(Long ownerId, String state, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userService.getUserById(ownerId);
        switch (BookingState.valueOf(state)) {
            case ALL:
                return bookingRepository.findAllByOwnerId(ownerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByOwnerId(ownerId, pageable).stream()
                        .map(BookingMapper::toBookingOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void bookingValidation(BookingDto bookingDto, User user, Item item) {

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    String.format("Дата начала: %s не может быть раньше текущего времени.",
                            bookingDto.getStart()));
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException(
                    String.format("Дата окончания: %s не может быть раньше даты начала: %s.",
                            bookingDto.getEnd(), bookingDto.getStart()));
        }
        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException(
                    String.format("Дата окончания: %s не может быть равна даты начала: %s.",
                            bookingDto.getEnd(), bookingDto.getStart()));
        }
        if (!item.getAvailable()) {
            throw new ValidationException(
                    String.format("Вещь с id %s не доступена.",
                            item.getId())
            );
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new NotFoundException(
                    String.format("Вещь с id %s не найдена.",
                            item.getId())
            );
        }
    }


    private Booking validateBookingDetails(Long userId, Long bookingId, Integer number) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);
        bookingById.orElseThrow(() -> new NotFoundException(String.format("Бронь с id %s не найдена.", bookingId)));
        log.error("Бронь с id {} не найдена.", bookingId);

        Booking booking = bookingById.get();
        switch (number) {
            case 1:
                if (!booking.getItem().getOwner().getId().equals(userId)) {
                    log.warn("Пользователь не является владельцем вещи");
                    throw new NotFoundException(String.format("Пользователь с id %s не является владельцем", userId));
                }
                if (!booking.getStatus().equals(BookingStatus.WAITING)) {
                    log.warn("В брони уже изменили статус");
                    throw new ValidationException(String.format("Бронь c id %s уже изменил статус",
                            booking.getId()));

                }
                return booking;
            case 2:
                if (!booking.getBooker().getId().equals(userId)
                        && !booking.getItem().getOwner().getId().equals(userId)) {
                    log.warn("Пользователь не является ни владельцем, ни автором бронирования");
                    throw new NotFoundException(String.format("Пользователь с id %s не является владельцем или автором бронирования ", userId));
                }
                return booking;
        }
        return null;
    }
}
