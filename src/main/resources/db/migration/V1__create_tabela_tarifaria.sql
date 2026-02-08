
-- Descrição: Cria a tabela principal de tarifas

CREATE TABLE IF NOT EXISTS tabela_tarifaria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    vigencia DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para tabelas ativas
CREATE INDEX idx_tabela_tarifaria_active ON tabela_tarifaria(active);

-- Comentários
COMMENT ON TABLE tabela_tarifaria IS 'Tabela principal de tarifas de água';
COMMENT ON COLUMN tabela_tarifaria.id IS 'Identificador único da tabela tarifária';
COMMENT ON COLUMN tabela_tarifaria.nome IS 'Nome da tabela tarifária (ex: Tabela 2024)';
COMMENT ON COLUMN tabela_tarifaria.vigencia IS 'Data de vigência da tabela';
COMMENT ON COLUMN tabela_tarifaria.active IS 'Flag indicando se a tabela está ativa';
COMMENT ON COLUMN tabela_tarifaria.created_at IS 'Data de criação do registro';
COMMENT ON COLUMN tabela_tarifaria.updated_at IS 'Data da última atualização';

