-- Migração para padronização de nomenclatura
-- Renomeia a coluna 'curiosidades' para 'curiosidade' (singular)

ALTER TABLE livros 
RENAME COLUMN curiosidades TO curiosidade;
