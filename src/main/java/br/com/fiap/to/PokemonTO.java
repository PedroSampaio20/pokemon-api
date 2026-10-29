package br.com.fiap.to;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * TO (Transfer Object): representa um Pokémon e é o objeto que "viaja" entre as camadas
 * (Resource -> BO -> DAO) e também entre a API e o cliente, convertido para/de JSON.
 *
 * As anotações do Bean Validation (aula 41) validam os dados recebidos no corpo da
 * requisição. Elas só entram em ação quando o Resource usa @Valid no parâmetro.
 */
public class PokemonTO {

    // chave primária: gerada automaticamente pelo banco (IDENTITY), por isso não é validada
    private Long codigo;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotNull(message = "A altura é obrigatória")
    @PositiveOrZero(message = "A altura não pode ser negativa")
    @Max(value = 999, message = "A altura deve ser no máximo 999 m")  // coluna ALTURA é NUMBER(5,2)
    private Double altura;

    @NotNull(message = "O peso é obrigatório")
    @PositiveOrZero(message = "O peso não pode ser negativo")
    @Max(value = 9999, message = "O peso deve ser no máximo 9999 kg")  // coluna PESO é NUMBER(6,2)
    private Double peso;

    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 100, message = "A categoria deve ter no máximo 100 caracteres")
    private String categoria;

    // não é possível capturar um Pokémon no futuro: a data deve ser hoje ou no passado
    @NotNull(message = "A data da captura é obrigatória")
    @PastOrPresent(message = "A data da captura não pode ser uma data futura")
    private LocalDate dataDaCaptura;

    // construtor vazio: usado pelo Spring (Jackson) para converter o JSON em objeto
    public PokemonTO() {
    }

    public PokemonTO(Long codigo, String nome, Double altura, Double peso, String categoria, LocalDate dataDaCaptura) {
        this.codigo = codigo;
        this.nome = nome;
        this.altura = altura;
        this.peso = peso;
        this.categoria = categoria;
        this.dataDaCaptura = dataDaCaptura;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDate getDataDaCaptura() {
        return dataDaCaptura;
    }

    public void setDataDaCaptura(LocalDate dataDaCaptura) {
        this.dataDaCaptura = dataDaCaptura;
    }
}
