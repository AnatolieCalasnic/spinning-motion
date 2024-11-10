CREATE TABLE app_user (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        fname VARCHAR(50) NOT NULL,
                        lname VARCHAR(50) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        address VARCHAR(255) NOT NULL,
                        postal_code VARCHAR(20) NOT NULL,
                        country VARCHAR(100) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        region VARCHAR(100),
                        phone_number VARCHAR(15) NOT NULL,
                        is_admin BOOLEAN NOT NULL DEFAULT FALSE,
                        PRIMARY KEY (id),
                        UNIQUE (email)
);

CREATE TABLE genre (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE record (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        title VARCHAR(255) NOT NULL,
                        artist VARCHAR(255) NOT NULL,
                        genre_id BIGINT,
                        price DECIMAL(10,2) NOT NULL,
                        release_year INT,
                        `condition` VARCHAR(50) NOT NULL,
                        quantity INT NOT NULL DEFAULT 0,
                        PRIMARY KEY (id),
                        FOREIGN KEY (genre_id) REFERENCES genre(id)
);

CREATE TABLE record_images (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               record_id BIGINT NOT NULL,
                               image_data MEDIUMBLOB,
                               image_type VARCHAR(50),
                               PRIMARY KEY (id),
                               FOREIGN KEY (record_id) REFERENCES record(id)
);