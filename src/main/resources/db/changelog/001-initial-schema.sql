--liquibase formatted sql

--changeset author:initial id:001-initial-schema
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    source_account_id BIGINT,
    destination_account_id BIGINT,
    created_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    executed_at TIMESTAMP,
    note TEXT,
    CONSTRAINT fk_operations_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_operations_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_operations_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_operations_approved_by FOREIGN KEY (approved_by_id) REFERENCES users(id)
);

CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_operation FOREIGN KEY (operation_id) REFERENCES operations(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_accounts_owner_id ON accounts(owner_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_operations_source_account_id ON operations(source_account_id);
CREATE INDEX idx_operations_destination_account_id ON operations(destination_account_id);
CREATE INDEX idx_operations_created_by_id ON operations(created_by_id);
CREATE INDEX idx_operations_status ON operations(status);
CREATE INDEX idx_documents_operation_id ON documents(operation_id);
