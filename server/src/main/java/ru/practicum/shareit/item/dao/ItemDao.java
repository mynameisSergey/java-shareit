package ru.practicum.shareit.item.dao;


import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> getItemById(Long itemId);

    List<Item> getAll(Long userId);

    List<Item> search(String text);
}