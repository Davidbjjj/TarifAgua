
-- 1. Criar tabela tarifária padrão
INSERT INTO tabela_tarifaria (nome, vigencia, active)
VALUES ('Tabela Oficial 2024-2025', '2024-01-01', true)
ON CONFLICT DO NOTHING;

-- 2. Buscar o ID da tabela criada
DO $$
DECLARE
    v_tabela_id BIGINT;
BEGIN
    SELECT id INTO v_tabela_id FROM tabela_tarifaria
    WHERE nome = 'Tabela Oficial 2024-2025' LIMIT 1;

    IF v_tabela_id IS NOT NULL THEN
        -- =====================================================
        -- CATEGORIA: INDUSTRIAL
        -- =====================================================
        INSERT INTO faixa_tarifaria (categoria, inicio, fim, valor_unitario, tabela_id)
        VALUES
            ('INDUSTRIAL', 0, 10, 1.00, v_tabela_id),
            ('INDUSTRIAL', 11, 20, 2.00, v_tabela_id),
            ('INDUSTRIAL', 21, 99999, 3.00, v_tabela_id)
        ON CONFLICT DO NOTHING;

        -- =====================================================
        -- CATEGORIA: COMERCIAL
        -- =====================================================
        INSERT INTO faixa_tarifaria (categoria, inicio, fim, valor_unitario, tabela_id)
        VALUES
            ('COMERCIAL', 0, 10, 1.50, v_tabela_id),
            ('COMERCIAL', 11, 20, 2.50, v_tabela_id),
            ('COMERCIAL', 21, 99999, 3.50, v_tabela_id)
        ON CONFLICT DO NOTHING;

        -- =====================================================
        -- CATEGORIA: PARTICULAR
        -- =====================================================
        INSERT INTO faixa_tarifaria (categoria, inicio, fim, valor_unitario, tabela_id)
        VALUES
            ('PARTICULAR', 0, 10, 1.25, v_tabela_id),
            ('PARTICULAR', 11, 20, 2.00, v_tabela_id),
            ('PARTICULAR', 21, 30, 3.00, v_tabela_id),
            ('PARTICULAR', 31, 99999, 4.00, v_tabela_id)
        ON CONFLICT DO NOTHING;

        -- =====================================================
        -- CATEGORIA: PUBLICO
        -- =====================================================
        INSERT INTO faixa_tarifaria (categoria, inicio, fim, valor_unitario, tabela_id)
        VALUES
            ('PUBLICO', 0, 10, 0.75, v_tabela_id),
            ('PUBLICO', 11, 20, 1.25, v_tabela_id),
            ('PUBLICO', 21, 99999, 2.00, v_tabela_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

