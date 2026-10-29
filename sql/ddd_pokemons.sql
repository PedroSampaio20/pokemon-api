-- Script de criacao da tabela usada pela API (rodar no Oracle SQL Developer)
-- O CODIGO e gerado automaticamente pelo banco (IDENTITY), por isso nao o enviamos no INSERT.

CREATE TABLE DDD_POKEMONS (
    CODIGO          NUMBER GENERATED ALWAYS AS IDENTITY,
    NOME            VARCHAR2(100) NOT NULL,
    ALTURA          NUMBER(5,2)   NOT NULL,  -- em metros
    PESO            NUMBER(6,2)   NOT NULL,  -- em quilos
    CATEGORIA       VARCHAR2(100) NOT NULL,
    DATA_DA_CAPTURA DATE          NOT NULL,
    PRIMARY KEY (CODIGO)
);

-- (Opcional) alguns registros iniciais para a apresentacao
INSERT INTO DDD_POKEMONS (NOME, ALTURA, PESO, CATEGORIA, DATA_DA_CAPTURA) VALUES ('Bulbasaur', 0.70, 6.90, 'Semente', DATE '2026-03-10');
INSERT INTO DDD_POKEMONS (NOME, ALTURA, PESO, CATEGORIA, DATA_DA_CAPTURA) VALUES ('Charmander', 0.60, 8.50, 'Lagarto', DATE '2026-04-22');
INSERT INTO DDD_POKEMONS (NOME, ALTURA, PESO, CATEGORIA, DATA_DA_CAPTURA) VALUES ('Squirtle', 0.50, 9.00, 'Tartaruguinha', DATE '2026-05-05');
COMMIT;

-- Para recomecar do zero: DROP TABLE DDD_POKEMONS;
