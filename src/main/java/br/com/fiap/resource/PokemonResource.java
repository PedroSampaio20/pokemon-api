package br.com.fiap.resource;

import br.com.fiap.bo.PokemonBO;
import br.com.fiap.to.PokemonTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * Resource: a "porta de entrada" da API. Recebe as requisições HTTP, chama o BO
 * e devolve a resposta com o código de status adequado (ResponseEntity).
 *
 * Endpoints (todos em /pokemon):
 *   GET    /pokemon           -> lista todos              (200 OK)
 *   GET    /pokemon/{codigo}  -> busca um pelo código     (200 OK | 404 Not Found)
 *   POST   /pokemon           -> cadastra                 (201 Created | 400 Bad Request)
 *   PUT    /pokemon/{codigo}  -> atualiza                 (201 Created | 400 Bad Request)
 *   DELETE /pokemon/{codigo}  -> apaga                    (204 No Content | 404 Not Found)
 */
@RestController
@RequestMapping("/pokemon")
public class PokemonResource {

    private PokemonBO pokemonBO = new PokemonBO();

    @GetMapping
    public ResponseEntity<ArrayList<PokemonTO>> findAll() {
        ArrayList<PokemonTO> resultado = pokemonBO.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(resultado);
    }

    // @PathVariable: pega o valor digitado na URI (ex.: /pokemon/3 -> codigo = 3)
    @GetMapping("/{codigo}")
    public ResponseEntity<?> findByCodigo(@PathVariable Long codigo) {
        PokemonTO pokemon = pokemonBO.findByCodigo(codigo);
        if (pokemon != null) {
            return ResponseEntity.status(HttpStatus.OK).body(pokemon);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pokémon não encontrado!");
        }
    }

    // @RequestBody: o JSON do corpo vira um PokemonTO | @Valid: aplica as regras do Bean Validation.
    // Se a validação falhar, o próprio Spring responde 400 Bad Request antes de entrar no método.
    @PostMapping
    public ResponseEntity<?> save(@RequestBody @Valid PokemonTO pokemon) {
        PokemonTO resultado = pokemonBO.save(pokemon);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao salvar Pokémon");
        }
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<?> update(@PathVariable Long codigo, @RequestBody @Valid PokemonTO pokemon) {
        // o código que vale é o da URI, não o do JSON
        pokemon.setCodigo(codigo);
        PokemonTO resultado = pokemonBO.update(pokemon);
        if (resultado != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar Pokémon");
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<String> delete(@PathVariable Long codigo) {
        if (pokemonBO.delete(codigo)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Pokémon deletado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pokémon não encontrado!");
        }
    }
}
