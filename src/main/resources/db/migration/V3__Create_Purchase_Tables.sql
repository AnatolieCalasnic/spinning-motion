CREATE TABLE purchase_history (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  record_id BIGINT NOT NULL,
                                  purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  status VARCHAR(50) NOT NULL,
                                  quantity INT NOT NULL,
                                  price DECIMAL(10,2) NOT NULL,
                                  total_amount DECIMAL(10,2) NOT NULL,
                                  PRIMARY KEY (id),
                                  FOREIGN KEY (user_id) REFERENCES app_user(id),
                                  FOREIGN KEY (record_id) REFERENCES record(id)
);

CREATE TABLE purchase_item (
                               purchase_id BIGINT NOT NULL,
                               record_id BIGINT NOT NULL,
                               quantity INT NOT NULL,
                               price DECIMAL(10,2) NOT NULL,
                               PRIMARY KEY (purchase_id, record_id),
                               FOREIGN KEY (purchase_id) REFERENCES purchase_history(id),
                               FOREIGN KEY (record_id) REFERENCES record(id)
);