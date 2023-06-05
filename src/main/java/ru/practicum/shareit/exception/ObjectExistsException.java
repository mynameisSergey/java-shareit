package ru.practicum.shareit.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ObjectExistsException extends RuntimeException {
    public ObjectExistsException(final String message) {
        super(message);
    }
}