#!/usr/bin/env bash
# =============================================================================
# Testa TODAS as requisições da API Pokémon pelo terminal, usando curl.
#
# Uso:
#   ./testar-api.sh                                        -> testa a API local (http://localhost:8080)
#   ./testar-api.sh https://SEU-SERVICO.onrender.com       -> testa a API publicada no Render
#
# A cada passo o script mostra a requisição, a resposta e o código de status HTTP,
# e espera você apertar ENTER para seguir (bom para ir explicando na apresentação).
# =============================================================================

BASE="${1:-http://localhost:8080}"
BASE="${BASE%/}/pokemon"

# faz uma requisição, imprime o corpo da resposta e o status; guarda o corpo em $RESPOSTA
requisicao() {
  local titulo="$1"; shift
  echo
  echo "=============================================================="
  echo "  $titulo"
  echo "  curl $*"
  echo "--------------------------------------------------------------"
  RESPOSTA=$(curl -s -w '\n%{http_code}' "$@")
  STATUS="${RESPOSTA##*$'\n'}"
  RESPOSTA="${RESPOSTA%$'\n'*}"
  echo "$RESPOSTA"
  echo "-> HTTP $STATUS"
  read -r -p "[ENTER para continuar]" _
}

echo "Testando a API em: $BASE"

# 1) GET - lista todos
requisicao "1) findAll  (GET /pokemon) - esperado 200" "$BASE"

# 2) POST - cadastra (esperado 201 com o código gerado)
requisicao "2) save  (POST /pokemon) - esperado 201" \
  -X POST "$BASE" -H "Content-Type: application/json" \
  -d '{"nome":"Pikachu","altura":0.4,"peso":6.0,"categoria":"Rato","dataDaCaptura":"2026-09-15"}'

# se o cadastro falhou, para aqui: senão os próximos passos alterariam/apagariam um Pokémon que já existia
if [ "$STATUS" != "201" ]; then
  echo "O cadastro não deu 201. Verifique se a API e o banco estão no ar."
  exit 1
fi

# o POST devolve o Pokémon sem o código (quem gera é o banco), então buscamos a lista
# (ordenada por código) e pegamos o último código: é o do Pokémon que acabamos de cadastrar
CODIGO=$(curl -s "$BASE" | grep -o '"codigo":[0-9]*' | tail -1 | cut -d: -f2)
if [ -z "$CODIGO" ]; then
  echo "Não foi possível obter o código do Pokémon cadastrado. Verifique se a API e o banco estão no ar."
  exit 1
fi
echo "Código do Pokémon cadastrado: $CODIGO"

# 3) POST inválido - Bean Validation recusa (esperado 400 com o erro de cada campo)
requisicao "3) save com dados inválidos (POST /pokemon) - esperado 400" \
  -X POST "$BASE" -H "Content-Type: application/json" \
  -d '{"nome":"","altura":-1,"peso":0,"categoria":"Rato","dataDaCaptura":"2099-01-01"}'

# 4) GET por código
requisicao "4) findByCodigo  (GET /pokemon/$CODIGO) - esperado 200" "$BASE/$CODIGO"

# 5) GET por código que não existe
requisicao "5) findByCodigo inexistente  (GET /pokemon/999999) - esperado 404" "$BASE/999999"

# 6) PUT - atualiza
requisicao "6) update  (PUT /pokemon/$CODIGO) - esperado 201" \
  -X PUT "$BASE/$CODIGO" -H "Content-Type: application/json" \
  -d '{"nome":"Raichu","altura":0.8,"peso":30.0,"categoria":"Rato","dataDaCaptura":"2026-09-15"}'

# 7) DELETE - apaga
requisicao "7) delete  (DELETE /pokemon/$CODIGO) - esperado 204" -X DELETE "$BASE/$CODIGO"

# 8) DELETE de novo - já não existe
requisicao "8) delete de novo  (DELETE /pokemon/$CODIGO) - esperado 404" -X DELETE "$BASE/$CODIGO"

# 9) CORS - o filtro responde ao "preflight" OPTIONS com os cabeçalhos Access-Control-*
echo
echo "=============================================================="
echo "  9) CorsFilter  (OPTIONS /pokemon) - esperado 200 + cabeçalhos Access-Control-*"
echo "  curl -i -X OPTIONS $BASE"
echo "--------------------------------------------------------------"
curl -s -i -X OPTIONS "$BASE" | grep -i -E "^HTTP|^access-control"

echo
echo "Fim dos testes."
