CREATE INDEX idx_record_genre ON record(genre_id);
CREATE INDEX idx_record_title_artist ON record(title, artist);
CREATE INDEX idx_review_record ON review(record_id);
CREATE INDEX idx_review_app_user ON review(user_id);
CREATE INDEX idx_purchase_history_app_user ON purchase_history(user_id);
CREATE INDEX idx_basket_item_record ON basket_item(record_id);