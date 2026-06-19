-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create account_balances table
CREATE TABLE account_balances (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_balances_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Create networth_snapshots table
CREATE TABLE networth_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_assets DECIMAL(19, 4) NOT NULL,
    total_liabilities DECIMAL(19, 4) NOT NULL,
    net_worth DECIMAL(19, 4) NOT NULL,
    snapshot_date DATE NOT NULL,
    CONSTRAINT fk_snapshots_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_snapshot_date UNIQUE (user_id, snapshot_date)
);

-- Create goals table
CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    deadline DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_category ON accounts(category);
CREATE INDEX idx_account_balances_account_id ON account_balances(account_id);
CREATE INDEX idx_account_balances_recorded_at ON account_balances(recorded_at);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_snapshots_user_id ON networth_snapshots(user_id);
CREATE INDEX idx_snapshots_date ON networth_snapshots(snapshot_date);
CREATE INDEX idx_goals_user_id ON goals(user_id);
CREATE INDEX idx_goals_status ON goals(status);
