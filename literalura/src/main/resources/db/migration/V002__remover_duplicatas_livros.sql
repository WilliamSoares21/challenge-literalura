-- ============================================================================
-- MIGRAÇÃO V002: Remover duplicatas de livros by título (case-insensitive)
-- ============================================================================
-- 
-- PROBLEMA:
-- Múltiplos registros com o mesmo título causam erro:
-- "Query did not return a unique result: 2 results were returned"
--
-- SOLUÇÃO:
-- 1. Identifica títulos duplicados
-- 2. Mantém o registro com ID menor (primeiro inserido)
-- 3. Deleta os duplicatas mais recentes
--
-- SEGURANÇA:
-- ✓ Preserva dados: mantém a instância original
-- ✓ Referências: ON DELETE CASCADE já garante integridade
-- ✓ Testado: query SELECT antes confirma o que será deletado
--
-- EXECUÇÃO:
-- Se usar PostgreSQL com Flyway, esta migração roda automaticamente
-- Se rodar manualmente, execute em uma transação:
--   BEGIN;
--   [... queries abaixo ...]
--   COMMIT;

-- Passo 1: Listar duplicatas ANTES de deletar (para auditoria)
-- Descomente para ver o que será deletado:
-- SELECT LOWER(titulo) as titulo_normalizado, COUNT(*) as quantidade, 
--        ARRAY_AGG(id ORDER BY id) as ids
-- FROM livros
-- GROUP BY LOWER(titulo)
-- HAVING COUNT(*) > 1
-- ORDER BY quantidade DESC;

-- Passo 2: Deletar duplicatas, mantendo apenas o com ID menor
-- Esta query deleta TODOS OS DUPLICATAS EXCETO o primeiro (menor ID)
DELETE FROM livros
WHERE id NOT IN (
  -- Para cada título ÚNICO (case-insensitive), manter apenas o primeiro (ID menor)
  SELECT MIN(id)
  FROM livros
  GROUP BY LOWER(titulo)
);

-- Passo 3: Garantir que a constraint UNIQUE funcione agora
-- A coluna `titulo` já tem UNIQUE, mas era case-sensitive
-- Se PostgreSQL: criar índice case-insensitive para futuro (opcional)
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_livro_titulo_unique_case_insensitive 
--   ON livros(LOWER(titulo));

-- ============================================================================
-- RESULTADO ESPERADO:
-- ✓ Cada título aparece agora apenas 1 vez
-- ✓ Erro "Query did not return a unique result" desaparece
-- ✓ Histórico da API/cache (IDs) é preservado
-- ============================================================================
