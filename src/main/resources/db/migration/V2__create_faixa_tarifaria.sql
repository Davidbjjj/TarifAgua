
-- Descrição: Cria a tabela de faixas de consumo com suas tarifas

CREATE TABLE IF NOT EXISTS faixa_tarifaria (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL,
    inicio INTEGER NOT NULL,
    fim INTEGER NOT NULL,
    valor_unitario NUMERIC(10, 2) NOT NULL,
    tabela_id BIGINT NOT NULL REFERENCES tabela_tarifaria(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX idx_faixa_tarifaria_tabela_id ON faixa_tarifaria(tabela_id);
CREATE INDEX idx_faixa_tarifaria_categoria ON faixa_tarifaria(categoria);
CREATE INDEX idx_faixa_tarifaria_tabela_categoria ON faixa_tarifaria(tabela_id, categoria);
CREATE INDEX idx_faixa_tarifaria_inicio ON faixa_tarifaria(inicio);

-- Restrição UNIQUE para evitar faixas duplicadas
CREATE UNIQUE INDEX idx_faixa_tarifaria_unique
    ON faixa_tarifaria(tabela_id, categoria, inicio, fim);

-- Comentários
COMMENT ON TABLE faixa_tarifaria IS 'Tabela de faixas de consumo com tarifas';
COMMENT ON COLUMN faixa_tarifaria.id IS 'Identificador único da faixa';
COMMENT ON COLUMN faixa_tarifaria.categoria IS 'Categoria de consumidor (INDUSTRIAL, COMERCIAL, PARTICULAR, PUBLICO)';
COMMENT ON COLUMN faixa_tarifaria.inicio IS 'Valor inicial da faixa em m³';
COMMENT ON COLUMN faixa_tarifaria.fim IS 'Valor final da faixa em m³';
COMMENT ON COLUMN faixa_tarifaria.valor_unitario IS 'Tarifa em R$/m³';
COMMENT ON COLUMN faixa_tarifaria.tabela_id IS 'Referência para tabela_tarifaria';
COMMENT ON COLUMN faixa_tarifaria.created_at IS 'Data de criação do registro';
COMMENT ON COLUMN faixa_tarifaria.updated_at IS 'Data da última atualização';

