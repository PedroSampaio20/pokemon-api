# Pokédex — API Restful com Spring Boot (FIAP · Domain Driven Design Using Java)

API Restful para cadastro de Pokémons (uma Pokédex), feita no "Praticando" das aulas 40 a 44
do Prof. Gilberto. Ela segue a mesma arquitetura do projeto `megafarma` das aulas:
**TO → DAO → BO → Resource**, com JDBC puro no Oracle, Bean Validation, CORS, build em `.jar` e deploy no Render via Docker.

---

## 1. O que tem neste projeto

```
.
├── pom.xml                         ← dependências Maven (Spring Boot 4.1.1, Java 21)
├── Dockerfile                      ← receita usada pelo Render para gerar e rodar o .jar (aula 44)
├── mvnw / mvnw.cmd / .mvn/         ← Maven Wrapper: compila sem precisar instalar o Maven
├── sql/ddd_pokemons.sql            ← script que cria a tabela no Oracle (+ 3 Pokémons de exemplo)
├── insomnia/
│   ├── pokemon-insomnia.json       ← coleção pronta com todas as requisições (importar no Insomnia)
│   ├── json-cadastro.txt           ← JSON de exemplo para o POST (dica da aula 40)
│   ├── json-atualizacao.txt        ← JSON de exemplo para o PUT (dica da aula 43)
│   └── json-invalido.txt           ← JSON que mostra o Bean Validation recusando dados
├── rodar-local.sh                  ← pede RM/senha e sobe a API conectada ao Oracle
├── testar-api.sh                   ← script que testa todas as requisições pelo terminal (curl)
├── frontend/index.html             ← front-end que consome a API (aula 44)
└── src/main/java/br/com/fiap/
    ├── PokemonApplication.java     ← classe que sobe a aplicação Spring
    ├── to/PokemonTO.java           ← o "Pokémon" (atributos + Bean Validation)
    ├── dao/ConnectionFactory.java  ← conexão com o Oracle (lê credenciais das variáveis de ambiente)
    ├── dao/PokemonDAO.java         ← SQL: findAll, findByCodigo, save, update, delete
    ├── bo/PokemonBO.java           ← regras de negócio (data da captura não pode ser futura)
    ├── resource/PokemonResource.java ← endpoints HTTP (GET, POST, PUT, DELETE)
    └── resource/CorsFilter.java    ← libera o front-end (outra origem) a chamar a API (CORS)
```

### Como as camadas conversam

```
Cliente (terminal com curl / Insomnia / front-end)
        │  HTTP + JSON
        ▼
PokemonResource  → recebe a requisição, valida com @Valid, devolve ResponseEntity + status
        ▼
PokemonBO        → aplica as regras de negócio
        ▼
PokemonDAO       → executa o SQL com PreparedStatement
        ▼
ConnectionFactory → abre/fecha a conexão com o Oracle
        ▼
Tabela DDD_POKEMONS
```

O `PokemonTO` é o objeto que passa por todas as camadas (e vira JSON na resposta).

### Conceitos de cada aula e onde estão

| Aula | Conceito | Onde está no projeto |
|------|----------|----------------------|
| 40 | Driver Oracle no `pom.xml` | `pom.xml` (`ojdbc17`) |
| 40 | `ConnectionFactory` (Factory + Singleton) | `dao/ConnectionFactory.java` |
| 40 | Tabela no Oracle | `sql/ddd_pokemons.sql` |
| 40 | `save()` no DAO, BO e Resource (POST, `@RequestBody`, 201/400) | `PokemonDAO`, `PokemonBO`, `PokemonResource` |
| 40 | Regra de negócio: data da captura no passado | `PokemonBO.dataDaCapturaInvalida()` |
| 41 | Bean Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@PastOrPresent`, `@Valid`) | `PokemonTO`, `PokemonResource` |
| 41 | `findAll()` buscando do banco | `PokemonDAO.findAll()` |
| 41 | `findByCodigo()` com `@GetMapping("/{codigo}")` e `@PathVariable` (200/404) | `PokemonResource.findByCodigo()` |
| 42 | `delete()` com `@DeleteMapping` (204/404) | `PokemonResource.delete()` |
| 43 | `update()` com `@PutMapping`, `@PathVariable`, `@RequestBody`, `@Valid` (201/400) | `PokemonResource.update()` |
| 44 | `CorsFilter` (pacote resource) | `resource/CorsFilter.java` |
| 44 | Front-end consumindo a API | `frontend/index.html` |
| 44 | Build do `.jar` (`./mvnw clean -DskipTests package`) | seção 5 deste README |
| 44 | Variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`) | `ConnectionFactory.getConnection()` |
| 44 | Dockerfile + GitHub + Render | `Dockerfile` e seção 6 deste README |

---

## 2. Endpoints da API

Base local: `http://localhost:8080` · Base no deploy: `https://SEU-SERVICO.onrender.com`

| Método | URI | Corpo | Sucesso | Falha |
|--------|-----|-------|---------|-------|
| GET | `/pokemon` | — | **200** + lista | — |
| GET | `/pokemon/{codigo}` | — | **200** + Pokémon | **404** "Pokémon não encontrado!" |
| POST | `/pokemon` | JSON | **201** + Pokémon cadastrado | **400** (validação ou erro ao salvar) |
| PUT | `/pokemon/{codigo}` | JSON | **201** + Pokémon atualizado | **400** (validação ou código inexistente) |
| DELETE | `/pokemon/{codigo}` | — | **204** (sem corpo) | **404** "Pokémon não encontrado!" |

JSON de exemplo (POST/PUT) — o `codigo` não é enviado, o banco gera sozinho:

```json
{
  "nome": "Pikachu",
  "altura": 0.4,
  "peso": 6.0,
  "categoria": "Rato",
  "dataDaCaptura": "2026-09-15"
}
```

Regras de validação (Bean Validation no `PokemonTO`; se alguma falhar, o Spring responde **400 Bad Request**):

- `nome` e `categoria`: obrigatórios (`@NotBlank`), até 100 caracteres (`@Size`)
- `altura` e `peso`: obrigatórios (`@NotNull`), não negativos (`@PositiveOrZero`) e com limite máximo (`@Max`: altura até 999, peso até 9999)
- `dataDaCaptura`: obrigatória (`@NotNull`), formato `AAAA-MM-DD`, não pode ser futura (`@PastOrPresent`)

As mensagens de cada regra aparecem no console da API (onde ela está rodando).

---

## 3. Criar a tabela no Oracle

> Se a tabela já existe no seu usuário, não precisa rodar de novo.

1. Abra o **Oracle SQL Developer** e conecte no servidor da FIAP (`oracle.fiap.com.br`, porta `1521`, SID `ORCL`, com seu RM e senha).
2. Abra o arquivo `sql/ddd_pokemons.sql` e execute tudo (F5 — "Executar script").
3. Confira: `SELECT * FROM DDD_POKEMONS;` deve mostrar Bulbasaur, Charmander e Squirtle.

---

## 4. Rodar a API no computador

Você precisa do **JDK 21** (o IntelliJ baixa sozinho em *File → Project Structure → SDK → Download JDK*).

A `ConnectionFactory` lê o acesso ao banco de **variáveis de ambiente** (assim a senha não vai para o GitHub):

| Variável | Valor |
|----------|-------|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | seu usuário (ex.: `rm123456`) |
| `DB_PASSWORD` | sua senha do Oracle |

**Jeito mais fácil (terminal):**

```bash
./rodar-local.sh
```

Ele pergunta seu usuário (RM) e senha do Oracle (a senha não aparece ao digitar e não é salva em arquivo),
coloca nas variáveis de ambiente e sobe a API. Para parar: `Ctrl+C`.

**Pelo IntelliJ:** *Run → Edit Configurations → PokemonApplication → Environment variables* e cole:

```
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL;DB_USER=rm123456;DB_PASSWORD=suasenha
```

Depois é só dar Run em `PokemonApplication`.

**Pelo terminal (macOS/Linux)**, dentro da pasta do projeto:

```bash
DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL" DB_USER="rm123456" DB_PASSWORD="suasenha" ./mvnw spring-boot:run
```

Teste no navegador: <http://localhost:8080/pokemon>

---

## 5. Build — gerar o `.jar` (aula 44)

```bash
./mvnw clean -DskipTests package
```

O executável fica em `target/pokemon-0.0.1-SNAPSHOT.jar`. Para rodar (com as mesmas variáveis de ambiente da seção 4):

```bash
DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL" DB_USER="rm123456" DB_PASSWORD="suasenha" java -jar target/pokemon-0.0.1-SNAPSHOT.jar
```

Para parar: `Ctrl+C`.

---

## 6. Deploy no Render (aula 44)

### 6.1 Subir para o GitHub

Crie um repositório **novo e vazio** no GitHub (ex.: `pokemon-api`) e, na pasta do projeto:

```bash
git add .
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/SUA_CONTA/pokemon-api.git
git push -u origin main
```

> O repositório já foi iniciado, por isso o `git init` da aula não aparece aqui. Nenhuma senha está no código — ela vai só no Render.

### 6.2 Criar o serviço no Render

1. Entre em <https://render.com> com sua conta do GitHub.
2. **New → Web Service** e escolha o repositório `pokemon-api`.
3. **Name:** algo único com seu RM (ex.: `pokemon-rm123456`).
4. **Language/Runtime:** Docker (o Render detecta o `Dockerfile` sozinho).
5. **Instance Type:** Free.
6. Em **Environment Variables** adicione `DB_URL`, `DB_USER` e `DB_PASSWORD` (mesmos valores da seção 4).
7. Clique em **Deploy Web Service** e aguarde alguns minutos (o primeiro build baixa todas as dependências).
8. Teste: `https://pokemon-rm123456.onrender.com/pokemon`

> No plano gratuito o serviço "dorme" depois de ~15 min sem uso; a primeira chamada depois disso demora uns 30–60 s.
> **Antes de apresentar, abra a URL uma vez para "acordar" a API.**

---

## 7. Testar com o Insomnia

1. Abra o Insomnia → **Import** (ou *Create → Import*) → escolha `insomnia/pokemon-insomnia.json`.
2. A coleção **API Pokemon** aparece com 6 requisições, em ordem:
   `findAll`, `findByCodigo`, `save`, `save (invalido)`, `update`, `delete`.
3. No canto superior esquerdo escolha o ambiente:
   - **Local** → `http://localhost:8080`
   - **Render (deploy)** → edite (ícone de engrenagem/“Manage Environments”) e troque `SEU-SERVICO` pela sua URL do Render.
4. Nas requisições `findByCodigo`, `update` e `delete`, troque o `1` no final da URL por um código que exista.

---

## 8. Testar pelo terminal

### 8.1 Script automático (todas as requisições em sequência)

O `testar-api.sh` faz, em ordem: listar, cadastrar, cadastrar inválido, buscar por código,
buscar código inexistente, atualizar, apagar, apagar de novo e o teste do CORS.
Ele mostra a resposta e o código de status de cada passo e espera um ENTER entre eles.

```bash
./testar-api.sh
```

Contra a API publicada no Render:

```bash
./testar-api.sh https://SEU-SERVICO.onrender.com
```

### 8.2 Comandos curl avulsos

Troque `http://localhost:8080` pela URL do Render para testar o deploy. O `-i` mostra o código de status na primeira linha.

Listar todos (GET):

```bash
curl -i http://localhost:8080/pokemon
```

Buscar por código (GET):

```bash
curl -i http://localhost:8080/pokemon/1
```

Cadastrar (POST):

```bash
curl -i -X POST http://localhost:8080/pokemon -H "Content-Type: application/json" -d '{"nome":"Pikachu","altura":0.4,"peso":6.0,"categoria":"Rato","dataDaCaptura":"2026-09-15"}'
```

Cadastrar com dados inválidos (mostra o Bean Validation → 400):

```bash
curl -i -X POST http://localhost:8080/pokemon -H "Content-Type: application/json" -d '{"nome":"","altura":-1,"peso":0,"categoria":"Rato","dataDaCaptura":"2099-01-01"}'
```

Atualizar (PUT):

```bash
curl -i -X PUT http://localhost:8080/pokemon/1 -H "Content-Type: application/json" -d '{"nome":"Ivysaur","altura":1.0,"peso":13.0,"categoria":"Semente","dataDaCaptura":"2026-03-10"}'
```

Apagar (DELETE):

```bash
curl -i -X DELETE http://localhost:8080/pokemon/1
```

Ver o `CorsFilter` em ação (cabeçalhos `Access-Control-*`):

```bash
curl -i -X OPTIONS http://localhost:8080/pokemon
```

---

## 9. Testar com o front-end

1. Abra o arquivo `frontend/index.html` no navegador (duplo clique).
2. No campo **URL da API** deixe `http://localhost:8080/pokemon` (local) ou troque por `https://SEU-SERVICO.onrender.com/pokemon` (deploy) e clique em **Listar todos**.
3. A página permite: listar (GET), buscar por código (GET /{codigo}), cadastrar (POST), editar (PUT) e apagar (DELETE).

O front-end está em outra "origem" (arquivo local) que a API; ele só funciona por causa do `CorsFilter`.

---

## 10. Roteiro sugerido para a apresentação

1. Mostrar a estrutura de pacotes (`to`, `dao`, `bo`, `resource`) e explicar o fluxo Resource → BO → DAO.
2. Mostrar o `PokemonTO` com as anotações do Bean Validation.
3. Mostrar a `ConnectionFactory` lendo as variáveis de ambiente e o painel *Environment* do Render.
4. No Insomnia, apontando para o **Render**:
   - `findAll` → 200
   - `save` → 201
   - `save (invalido)` → 400 (Bean Validation recusou os dados)
   - `findByCodigo` → 200, e com um código inexistente → 404
   - `update` → 201
   - `delete` → 204, e repetir → 404
   (ou, no terminal, rodar `./testar-api.sh https://SEU-SERVICO.onrender.com`, que faz a mesma sequência)
5. Abrir o front-end apontando para o Render e cadastrar/editar/apagar.
6. Mostrar o `CorsFilter` com `curl -i -X OPTIONS .../pokemon` e explicar que ele libera navegadores
   de outras origens a chamar a API.

---

## 11. Problemas comuns

| Sintoma | Causa provável / solução |
|---------|--------------------------|
| `RuntimeException: Variáveis de ambiente do banco não configuradas!` | Faltou definir `DB_URL`, `DB_USER` ou `DB_PASSWORD` (seção 4 ou Render). |
| `GET /pokemon` volta `[]` | A tabela está vazia ou foi criada em outro usuário do Oracle. Rode o `sql/ddd_pokemons.sql` com o mesmo usuário da API. |
| `Erro na consulta: ORA-00942` no console | A tabela `DDD_POKEMONS` não existe nesse usuário. |
| `Erro de SQL: ... ORA-01017` | Usuário ou senha do Oracle incorretos. |
| Insomnia responde 415 | Faltou escolher **Body → JSON** na requisição. |
| `curl: (7) Failed to connect` | API não está rodando ou a URL/porta está errada. |
| `./testar-api.sh: Permission denied` | Rode `chmod +x testar-api.sh` uma vez. |
| Front-end mostra "Não foi possível conectar na API." | API não está rodando, URL errada, ou o Render ainda está "acordando". |
| curl responde 415 | Faltou o `-H "Content-Type: application/json"`. |
| Primeira chamada no Render demora | Normal no plano gratuito (serviço dormindo). |
