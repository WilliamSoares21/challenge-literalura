#!/bin/bash
# ========================================================================
# Script de Migração - LiterAlura
# ========================================================================
# Este script aplica a migração da coluna 'curiosidades' para 'curiosidade'
# ========================================================================

set -e  # Para em caso de erro

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║      Migração LiterAlura - Renomear coluna 'curiosidades'     ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Verifica se variáveis de ambiente estão definidas
if [ -z "$DB_LITERALURA_USER" ]; then
    echo "❌ Erro: Variável DB_LITERALURA_USER não definida"
    exit 1
fi

if [ -z "$DB_LITERALURA_PASSWORD" ]; then
    echo "❌ Erro: Variável DB_LITERALURA_PASSWORD não definida"
    exit 1
fi

# Diretório do script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="$SCRIPT_DIR/src/main/resources/db/migration/V001__renomear_coluna_curiosidades.sql"

# Verifica se arquivo SQL existe
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ Erro: Arquivo de migração não encontrado: $SQL_FILE"
    exit 1
fi

echo "📋 Informações da migração:"
echo "   Banco de dados: literalura"
echo "   Usuário: $DB_LITERALURA_USER"
echo "   Arquivo SQL: $SQL_FILE"
echo ""

# Confirmação
read -p "🔄 Deseja executar a migração? (s/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "❌ Migração cancelada"
    exit 0
fi

echo ""
echo "⏳ Executando migração..."
echo ""

# Executa a migração
PGPASSWORD=$DB_LITERALURA_PASSWORD psql \
    -U $DB_LITERALURA_USER \
    -d literalura \
    -f "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migração executada com sucesso!"
    echo ""
    echo "📊 Verificando estrutura da tabela..."
    echo ""
    
    # Verifica a estrutura da tabela
    PGPASSWORD=$DB_LITERALURA_PASSWORD psql \
        -U $DB_LITERALURA_USER \
        -d literalura \
        -c "\d livros" \
        -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'livros' AND column_name = 'curiosidade';"
    
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                  ✅ MIGRAÇÃO CONCLUÍDA                         ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Próximos passos:"
    echo "  1. Reinicie a aplicação Spring Boot"
    echo "  2. Teste os endpoints da API"
    echo "  3. Verifique os logs para garantir que tudo está funcionando"
    echo ""
else
    echo ""
    echo "❌ Erro ao executar a migração"
    exit 1
fi
