CREATE TABLE review (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         record_id BIGINT NOT NULL,
                         rating INT NOT NULL,
                         comment TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         FOREIGN KEY (user_id) REFERENCES app_user(id),
                         FOREIGN KEY (record_id) REFERENCES record(id),
                         UNIQUE (user_id, record_id)
);

CREATE TABLE basket (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         PRIMARY KEY (id),
                         FOREIGN KEY (user_id) REFERENCES app_user(id),
                         UNIQUE (user_id)
);

CREATE TABLE basket_item (
                              basket_id BIGINT NOT NULL,
                              record_id BIGINT NOT NULL,
                              quantity INT NOT NULL,
                              PRIMARY KEY (basket_id, record_id),
                              FOREIGN KEY (basket_id) REFERENCES basket(id),
                              FOREIGN KEY (record_id) REFERENCES record(id)
);