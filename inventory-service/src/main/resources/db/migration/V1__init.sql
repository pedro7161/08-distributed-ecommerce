CREATE TABLE inventory_item (product_id UUID PRIMARY KEY, available_quantity INTEGER NOT NULL, reserved_quantity INTEGER NOT NULL);
CREATE TABLE inventory_reservation (id UUID PRIMARY KEY, order_id UUID NOT NULL UNIQUE, product_id UUID NOT NULL, quantity INTEGER NOT NULL, status VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL, released_at TIMESTAMPTZ);
CREATE TABLE inbox_event (event_id UUID PRIMARY KEY, event_type VARCHAR(128) NOT NULL, received_at TIMESTAMPTZ NOT NULL, processed_at TIMESTAMPTZ);
CREATE TABLE outbox_event (id UUID PRIMARY KEY, aggregate_type VARCHAR(128) NOT NULL, aggregate_id VARCHAR(128) NOT NULL, type VARCHAR(128) NOT NULL, payload JSONB NOT NULL, occurred_at TIMESTAMPTZ NOT NULL);
INSERT INTO inventory_item(product_id,available_quantity,reserved_quantity) VALUES ('11111111-1111-1111-1111-111111111111',10,0);
