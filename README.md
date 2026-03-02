# Literalura 📚

> **Desafio Oracle Next Education | Alura:** Um catálogo inteligente de livros que consome a [API Gutendex](https://gutendex.com/) e traz funcionalidades avançadas para consulta e análise de dados literários.

![Java](https://img.shields.io/badge/Java-21-007396?logo=java&style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=spring-boot&style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&style=for-the-badge)

---

## 📑 Tabela de Conteúdos

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades Principais](#funcionalidades-principais)
- [Destaques Técnicos](#destaques-técnicos)
- [Tecnologias](#tecnologias)
- [Como Rodar o Projeto](#como-rodar-o-projeto)
- [Exemplo de Uso](#exemplo-de-uso)
- [Autor](#autor)

---

## 📝 Sobre o Projeto

O **Literalura** é uma aplicação Java desenvolvida com Spring Boot, cujo objetivo é possibilitar buscas, registros e estatísticas sobre livros da API Gutendex, persistindo dados de forma eficiente via PostgreSQL. O projeto foi desenhado com foco em aprendizado colocar em pratica o conteudo abordado.

---

## 🚀 Funcionalidades Principais

- 🔍 **Buscar livros por título:** Consulta à API Gutendex e persistência automática no banco de dados.
- 🗂️ **Listar livros e autores:** Visualização completa dos registros armazenados.
- 🕵️ **Buscar autores vivos em determinado ano:** Analisa datas de nascimento/morte via banco.
- 🌐 **Listar livros por idioma:** Suporte para PT, EN, ES, FR.
- 👤 **Buscar autor por nome:** Consulta direta e eficiente ao banco.
- 🏆 **Top 10 livros mais baixados:** Estatístico via ranking.
- 📊 **Estatísticas de downloads:** Geração com `DoubleSummaryStatistics` e relatórios em tempo real.

---

## 🛠️ Destaques Técnicos

| Técnica / Diferencial                    | Descrição                                                                                           |
| ---------------------------------------- | --------------------------------------------------------------------------------------------------- |
| **JOIN FETCH (JPA)**                     | Evita o problema de performance N+1 ao consultar autores e seus livros, garantindo consultas otimizadas. |
| **Case Insensitivity (Banco)**           | Configuração do banco para impedir títulos duplicados via insensibilidade de maiúsculas/minúsculas.      |
| **DTOs (Records)**                       | Utilização de DTOs Java para manipulação segura dos dados da API.                                   |
| **Sanitização de Inputs**                | Uso de `URLEncoder` e tratamento de strings para garantir entradas seguras e aderentes aos padrões. |
| **Commits Atômicos & Padronizados**      | Histórico de desenvolvimento limpo, rastreável e seguro.                                            |

---

## 🧰 Tecnologias

- Java 21
- Spring Boot (3.x)
- Spring Data JPA
- PostgreSQL (15+)
- Maven

---

## ▶️ Como Rodar o Projeto

1. **Pré-requisitos:**
   - JDK 21
   - PostgreSQL instalado e operacional
   - Maven 3.8+

2. **Configuração do Banco de Dados:**

   Crie um banco chamado `literalura` e configure o usuário. Configure as variáveis de ambiente no seu sistema ou arquivo `.env`:

   ```bash
   export DB_LITERALURA_USER=seu_usuario
   export DB_LITERALURA_PASSWORD=sua_senha
   ```

   Ou, insira no `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/literalura
   spring.datasource.username=${DB_LITERALURA_USER}
   spring.datasource.password=${DB_LITERALURA_PASSWORD}
   ```

3. **Instale as dependências e execute:**

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## 💡 Exemplo de Uso

Menu apresentado no console (CLI):

```text
===== Bem-vindo ao Literalura =====

1 - Buscar livro por título
2 - Listar todos os livros cadastrados
3 - Listar autores cadastrados
4 - Exibir autores vivos em determinado ano
5 - Listar livros por idioma
6 - Buscar autor por nome
7 - Exibir Top 10 livros mais baixados
8 - Estatísticas de downloads
9 - Sair
===================================

Escolha uma opção:
```

---

## 👤 Autor

| Nome              | GitHub                                                | LinkedIn                                     |
| ----------------- | ----------------------------------------------------- | -------------------------------------------- |
| William Soares    | [WilliamSoares21](https://github.com/WilliamSoares21) | [Perfil](https://www.linkedin.com/in/william-soares/) |

---

**Desenvolvido por William Soares como parte do Challenge ONE | Alura | Oracle Next Education.**