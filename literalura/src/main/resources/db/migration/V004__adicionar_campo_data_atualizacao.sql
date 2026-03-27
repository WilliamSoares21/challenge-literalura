-- Migration: Adiciona campo de controle de atualização de dados
-- Propósito: Rastrear quando os dados do livro foram atualizados pela última vez
-- Casos de uso: Re-atualizar livros com dados desatualizados, cache inteligente

ALTER TABLE livros ADD COLUMN data_ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Índice para melhorar performance de busca por livros desatualizados
CREATE INDEX idx_livros_data_atualizacao ON livros(data_ultima_atualizacao);

COMMENT ON COLUMN livros.data_ultima_atualizacao IS 'Timestamp da última atualização dos dados do livro (imagem, resumo, gênero)';
