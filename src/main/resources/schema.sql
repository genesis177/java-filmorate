CREATE TABLE IF NOT EXISTS USERS (
   id   int not null primary key auto_increment,
    email VARCHAR(255),
    login VARCHAR(255),
    name VARCHAR(255),
    birthday DATE
);

create table if not exists FILMS (
    id   int not null primary key auto_increment,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INT,
    mpa_id INT,
    genres VARCHAR(255)
);

create table if not exists GENRES (
    id   int not null primary key auto_increment,
    name VARCHAR(255)
);

create table if not exists MPA (
   id   int not null primary key auto_increment,
    name VARCHAR(255)
);

create table if not exists FILM_GENRES (
   id   int not null primary key auto_increment,
    film_id BIGINT,
    genre_id INT,
    foreign key (film_id) references films(id),
    foreign key (genre_id) references genres(id)
);

CREATE TABLE IF NOT EXISTS FRIENDS (
  id int not null auto_increment,
  user_id BIGINT,
  friend_id BIGINT,
  status VARCHAR(20) DEFAULT 'PENDING',
  request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES USERS(id),
  FOREIGN KEY (friend_id) REFERENCES USERS(id)
);