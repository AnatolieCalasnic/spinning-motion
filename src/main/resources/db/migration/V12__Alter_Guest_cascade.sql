ALTER TABLE guest_order
DROP FOREIGN KEY guest_order_ibfk_1;

ALTER TABLE guest_order
ADD CONSTRAINT guest_order_ibfk_1
FOREIGN KEY (purchase_history_id)
REFERENCES purchase_history(id)
ON DELETE CASCADE;