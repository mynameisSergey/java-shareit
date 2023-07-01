package ru.practicum.shareit.booking.model;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState from(String bookingState) {
        for (BookingState value : BookingState.values()) {
            if (value.name().equals(bookingState)) {
                return value;
            }
        }
        return null;
    }
}