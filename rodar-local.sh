#!/usr/bin/env bash
# =============================================================================
# Sobe a API no computador, conectada ao Oracle da FIAP.
#
# Uso:  ./rodar-local.sh
#
# Pede seu usuário (RM) e senha do Oracle e guarda nas variáveis de ambiente
# DB_URL, DB_USER e DB_PASSWORD, que são lidas pela ConnectionFactory (aula 44).
# A senha não aparece na tela e não é salva em nenhum arquivo.
#
# Antes da primeira vez, crie a tabela rodando sql/ddd_pokemons.sql no SQL Developer.
# Precisa do JDK 21 instalado. A API sobe em http://localhost:8080/pokemon (Ctrl+C para parar).
# =============================================================================
cd "$(dirname "$0")"

export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
read -r -p "Usuário do Oracle (ex.: rm123456): " DB_USER
read -r -s -p "Senha do Oracle (não aparece ao digitar): " DB_PASSWORD
echo
export DB_USER DB_PASSWORD

./mvnw spring-boot:run
