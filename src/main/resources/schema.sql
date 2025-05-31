    CREATE TABLE IF NOT EXISTS USERS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255),
    login VARCHAR(255),
    name VARCHAR(255),
    birthday DATE
    );

    CREATE TABLE IF NOT EXISTS FILMS (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INT,
    mpa_id INT
    );

    CREATE TABLE IF NOT EXISTS GENRES (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255)
    );

    CREATE TABLE IF NOT EXISTS MPA (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255)
    );

    CREATE TABLE IF NOT EXISTS FILM_LIKES (
    film_id INT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES FILMS(id),
    FOREIGN KEY (user_id) REFERENCES USERS(id)
    );

    CREATE TABLE IF NOT EXISTS FRIENDS (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_time TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES USERS(id),
    FOREIGN KEY (friend_id) REFERENCES USERS(id)
    );

    CREATE TABLE IF NOT EXISTS FILM_GENRES (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES FILMS(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES GENRES(id) ON DELETE CASCADE
    );