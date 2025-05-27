-- Таблица жанров
CREATE TABLE IF NOT EXISTS GENRES (
    id INT PRIMARY KEY,
    name VARCHAR(255)
);

-- Таблица рейтингов MPAA
CREATE TABLE IF NOT EXISTS MPA (
    id INT PRIMARY KEY,
    name VARCHAR(255)
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS USERS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS FILMS (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    description VARCHAR(200),
    releaseDate DATE,
    duration INT,
    mpaId INT,
    FOREIGN KEY (mpaId) REFERENCES MPA(id)
);

-- Таблица связей фильмов и жанров
CREATE TABLE IF NOT EXISTS FILM_GENRES (
    film_id INT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES FILMS(id),
    FOREIGN KEY (genre_id) REFERENCES GENRES(id)
);

-- Таблица дружеских заявок
CREATE TABLE IF NOT EXISTS FRIENDS (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES USERS(id),
    FOREIGN KEY (friend_id) REFERENCES USERS(id)
);