CREATE TABLE guest_order (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             purchase_history_id BIGINT NOT NULL,
                             fname VARCHAR(50) NOT NULL,
                             lname VARCHAR(50) NOT NULL,
                             email VARCHAR(255) NOT NULL,
                             address VARCHAR(255) NOT NULL,
                             postal_code VARCHAR(20) NOT NULL,
                             country VARCHAR(100) NOT NULL,
                             city VARCHAR(100) NOT NULL,
                             region VARCHAR(100),
                             phonenum VARCHAR(15) NOT NULL,
                             FOREIGN KEY (purchase_history_id) REFERENCES purchase_history(id)
);