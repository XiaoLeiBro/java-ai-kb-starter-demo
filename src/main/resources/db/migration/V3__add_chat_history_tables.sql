CREATE TABLE conversations (
    id VARCHAR(36) PRIMARY KEY,
    owner_id VARCHAR(36) NOT NULL,
    knowledge_base_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conv_owner_status_updated ON conversations(owner_id, status, updated_at DESC);
CREATE INDEX idx_conv_owner_kb_status ON conversations(owner_id, knowledge_base_id, status);

CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_msg_conv_created ON messages(conversation_id, created_at ASC);

CREATE TABLE invocation_logs (
    id VARCHAR(36) PRIMARY KEY,
    owner_id VARCHAR(36) NOT NULL,
    knowledge_base_id VARCHAR(36),
    conversation_id VARCHAR(36),
    message_id VARCHAR(36),
    model_name VARCHAR(100) NOT NULL,
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inv_owner_created ON invocation_logs(owner_id, created_at DESC);
CREATE INDEX idx_inv_owner_kb_created ON invocation_logs(owner_id, knowledge_base_id, created_at DESC);
CREATE INDEX idx_inv_conversation ON invocation_logs(conversation_id);
