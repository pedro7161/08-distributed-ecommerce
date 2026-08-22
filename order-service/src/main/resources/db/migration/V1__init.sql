CREATE TABLE orders (
  id UUID PRIMARY KEY,
  correlation_id UUID NOT NULL,
  customer_email VARCHAR(320) NOT NULL,
  product_id UUID NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  unit_price NUMERIC(19,2) NOT NULL,
  total_amount NUMERIC(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  cancellation_reason VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_orders_correlation ON orders(correlation_id);

CREATE TABLE inbox_event (
  event_id UUID PRIMARY KEY,
  event_type VARCHAR(128) NOT NULL,
  received_at TIMESTAMPTZ NOT NULL,
  processed_at TIMESTAMPTZ
);

CREATE TABLE outbox_event (
  id UUID PRIMARY KEY,
  aggregate_type VARCHAR(128) NOT NULL,
  aggregate_id VARCHAR(128) NOT NULL,
  type VARCHAR(128) NOT NULL,
  payload JSONB NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_order_outbox_occurred_at ON outbox_event(occurred_at);
