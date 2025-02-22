# Share It
Используемые технологии и инструменты:
- Java 11, Spring Boot, Spring Data JPA, Hibernate, Docker, JUnit, Mockito

## Кратко о приложении
Приложение Share It предназначено для шеринга (от англ. share — «делиться») вещей.
Оно предоставляет пользователям возможность рассказывать, какими вещами они готовы поделиться, а также находить нужную вещь и брать её в аренду.
Сервис позволяет бронировать вещь на определённые даты, но и закрывать к ней доступ на время бронирования от других желающих.
На случай, если нужной вещи сервисе нет, у пользователей есть возможность оставлять запросы. По запросу можно добавлять новые вещи для шеринга.


## Каркас приложения
### Вещь
Основная сущность сервиса — **вещь**. В коде она фигурирует как Item.
Пользователь, который добавляет в приложение новую вещь, считается её **владельцем**. При добавлении вещи есть возможность указать её краткое название и добавить небольшое описание. Также у вещи есть **статус** — доступна ли она для аренды. Статус проставляет владелец.
### Бронирование
Для поиска вещей организован **поиск**. Чтобы воспользоваться нужной вещью, её требуется забронировать. Бронирование (Booking) — ещё одна важная сущность приложения. Бронируется вещь всегда на определённые даты. Владелец вещи обязательно должен подтвердить бронирование.
После того как вещь возвращена, у пользователя, который её арендовал, есть возможность оставить отзыв.
### Запрос вещи
Еще одна из сущностей - запрос вещи. Пользователь создаёт запрос если нужная ему вещь не найдена при поиске. В запросе указывается, что именно он ищет. В ответ на запрос другие пользовали могут добавить нужную вещь.
