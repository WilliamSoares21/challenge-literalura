-- Migration V003: Adicionar novos campos à tabela de livros
-- Adiciona imagem, resumo e gênero para enriquecer dados dos livros

ALTER TABLE livros
ADD COLUMN imagem VARCHAR(500) NULL COMMENT 'URL da imagem do livro (formato JPG da API Gutendex)',
ADD COLUMN resumo TEXT NULL COMMENT 'Resumo/sinopse do livro (primeiro item de summaries)',
ADD COLUMN genero VARCHAR(500) NULL COMMENT 'Gêneros/subjects concatenados do livro';

-- Criação de índice para melhorar buscas por gênero
CREATE INDEX idx_livros_genero ON livros(genero);