ALTER TABLE purchase_history
DROP FOREIGN KEY purchase_history_ibfk_2;

ALTER TABLE purchase_history
ADD CONSTRAINT purchase_history_ibfk_2
FOREIGN KEY (record_id)
REFERENCES record(id)
ON DELETE CASCADE;
