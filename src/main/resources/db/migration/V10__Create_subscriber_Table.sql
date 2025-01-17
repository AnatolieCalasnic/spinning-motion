CREATE TABLE subscriber (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             email VARCHAR(255) NOT NULL UNIQUE,
                             subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT uk_subscriber_email UNIQUE (email)
);