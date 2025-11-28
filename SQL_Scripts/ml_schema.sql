-- ============================================================================
--  SCHEMA für ML & Portfolio-Analytics
--  Ziel: Datenbasis für Risiko- & Trendmodelle
-- ============================================================================

-- Optional: eigenes Schema
CREATE SCHEMA IF NOT EXISTS brokerml;
SET
search_path TO brokerml, public;

-- ============================================================================
-- 1) Preisdaten (historische Preise)
-- ============================================================================

CREATE TABLE IF NOT EXISTS prices
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    symbol
    TEXT
    NOT
    NULL,
    ts
    TIMESTAMPTZ
    NOT
    NULL,
    open
    DOUBLE
    PRECISION,
    high
    DOUBLE
    PRECISION,
    low
    DOUBLE
    PRECISION,
    close
    DOUBLE
    PRECISION
    NOT
    NULL,
    volume
    DOUBLE
    PRECISION,
    source
    TEXT
    DEFAULT
    'alpaca'
);

-- Jede Kerze pro Symbol/Zeitpunkt nur einmal
CREATE UNIQUE INDEX IF NOT EXISTS ux_prices_symbol_ts
    ON prices(symbol, ts);

CREATE INDEX IF NOT EXISTS ix_prices_symbol_ts
    ON prices(symbol, ts DESC);


-- ============================================================================
-- 2) Portfolio-Snapshots (Tages-/Zeitpunkt-Übersicht)
-- ============================================================================

CREATE TABLE IF NOT EXISTS portfolio_snapshots
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    snapshot_ts
    TIMESTAMPTZ
    NOT
    NULL
    DEFAULT
    NOW
(
),
    equity NUMERIC
(
    18,
    4
), -- Gesamtdepotwert
    cash NUMERIC
(
    18,
    4
),
    pnl_day NUMERIC
(
    18,
    4
), -- Tagesgewinn/-verlust
    pnl_total NUMERIC
(
    18,
    4
), -- Gesamtgewinn/-verlust
    currency TEXT DEFAULT 'USD'
    );

CREATE INDEX IF NOT EXISTS ix_portfolio_snapshots_ts
    ON portfolio_snapshots(snapshot_ts DESC);


-- ============================================================================
-- 3) Positionen (zum Snapshot-Zeitpunkt)
-- ============================================================================

CREATE TABLE IF NOT EXISTS positions
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    snapshot_id
    BIGINT
    NOT
    NULL
    REFERENCES
    portfolio_snapshots
(
    id
) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    qty NUMERIC
(
    18,
    4
) NOT NULL,
    avg_price NUMERIC
(
    18,
    6
),
    market_value NUMERIC
(
    18,
    4
),
    sector TEXT
    );

CREATE INDEX IF NOT EXISTS ix_positions_snapshot
    ON positions(snapshot_id);

CREATE INDEX IF NOT EXISTS ix_positions_symbol
    ON positions(symbol);


-- ============================================================================
-- 4) Orders (historische Orderdaten – optional, aber nützlich)
-- ============================================================================

CREATE TABLE IF NOT EXISTS orders
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    broker_order_id
    TEXT, -- z.B. Alpaca-ID
    symbol
    TEXT
    NOT
    NULL,
    side
    TEXT
    CHECK (
    side
    IN
(
    'buy',
    'sell'
)),
    qty NUMERIC
(
    18,
    4
) NOT NULL,
    type TEXT, -- market, limit, ...
    status TEXT, -- filled, cancelled, ...
    submitted_at TIMESTAMPTZ,
    filled_at TIMESTAMPTZ,
    limit_price NUMERIC
(
    18,
    6
),
    average_fill_price NUMERIC
(
    18,
    6
)
    );

CREATE INDEX IF NOT EXISTS ix_orders_symbol
    ON orders(symbol);

CREATE INDEX IF NOT EXISTS ix_orders_status
    ON orders(status);


-- ============================================================================
-- 5) Trades / Fills (optional, detaillierte Ausführungsdaten)
-- ============================================================================

CREATE TABLE IF NOT EXISTS fills
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    order_id
    BIGINT
    REFERENCES
    orders
(
    id
) ON DELETE SET NULL,
    symbol TEXT NOT NULL,
    qty NUMERIC
(
    18,
    4
) NOT NULL,
    price NUMERIC
(
    18,
    6
) NOT NULL,
    side TEXT CHECK
(
    side
    IN
(
    'buy',
    'sell'
)),
    executed_at TIMESTAMPTZ NOT NULL
    );

CREATE INDEX IF NOT EXISTS ix_fills_symbol
    ON fills(symbol);

CREATE INDEX IF NOT EXISTS ix_fills_executed_at
    ON fills(executed_at DESC);


-- ============================================================================
-- 6) Hilfstabelle für ML-Modelle (Versionierung, Metadaten)
-- ============================================================================

CREATE TABLE IF NOT EXISTS ml_models
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    model_name
    TEXT
    NOT
    NULL,
    version
    TEXT
    NOT
    NULL,
    trained_at
    TIMESTAMPTZ
    NOT
    NULL
    DEFAULT
    NOW
(
),
    metrics JSONB, -- z.B. {"rmse":0.12,"r2":0.87}
    notes TEXT
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_ml_models_name_version
    ON ml_models(model_name, version);
