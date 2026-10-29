package br.com.fiap.dao;

import br.com.fiap.to.PokemonTO;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO (Data Access Object): única camada que conversa com o banco de dados.
 * Cada método monta um comando SQL, executa via JDBC (PreparedStatement) e
 * converte o resultado em objetos PokemonTO.
 *
 * Convenção das aulas: em caso de falha o método retorna null (ou false no delete).
 */
public class PokemonDAO {

    // GET /pokemon (aula 41): lista todos os Pokémons cadastrados
    public ArrayList<PokemonTO> findAll() {
        ArrayList<PokemonTO> pokemons = new ArrayList<PokemonTO>();
        String sql = "select * from ddd_pokemons order by codigo";
        try (PreparedStatement ps = ConnectionFactory.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    PokemonTO pokemon = new PokemonTO();
                    pokemon.setCodigo(rs.getLong("codigo"));
                    pokemon.setNome(rs.getString("nome"));
                    pokemon.setAltura(rs.getDouble("altura"));
                    pokemon.setPeso(rs.getDouble("peso"));
                    pokemon.setCategoria(rs.getString("categoria"));
                    pokemon.setDataDaCaptura(rs.getDate("data_da_captura").toLocalDate());
                    pokemons.add(pokemon);
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Erro na consulta: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return pokemons;
    }

    // GET /pokemon/{codigo} (aula 41): busca um Pokémon pela chave primária
    public PokemonTO findByCodigo(Long codigo) {
        PokemonTO pokemon = new PokemonTO();
        String sql = "select * from ddd_pokemons where codigo = ?";
        try (PreparedStatement ps = ConnectionFactory.getConnection().prepareStatement(sql)) {
            ps.setLong(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pokemon.setCodigo(rs.getLong("codigo"));
                pokemon.setNome(rs.getString("nome"));
                pokemon.setAltura(rs.getDouble("altura"));
                pokemon.setPeso(rs.getDouble("peso"));
                pokemon.setCategoria(rs.getString("categoria"));
                pokemon.setDataDaCaptura(rs.getDate("data_da_captura").toLocalDate());
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Erro na consulta: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return pokemon;
    }

    // POST /pokemon (aula 40): cadastra um novo Pokémon.
    // O código não é enviado: o banco gera sozinho (IDENTITY).
    public PokemonTO save(PokemonTO pokemon) {
        String sql = "insert into ddd_pokemons (nome, altura, peso, categoria, data_da_captura) values (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConnectionFactory.getConnection().prepareStatement(sql)) {
            ps.setString(1, pokemon.getNome());
            ps.setDouble(2, pokemon.getAltura());
            ps.setDouble(3, pokemon.getPeso());
            ps.setString(4, pokemon.getCategoria());
            ps.setDate(5, Date.valueOf(pokemon.getDataDaCaptura()));
            if (ps.executeUpdate() > 0) {
                return pokemon;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // PUT /pokemon/{codigo} (aula 43): atualiza um Pokémon existente
    public PokemonTO update(PokemonTO pokemon) {
        String sql = "update ddd_pokemons set nome=?, altura=?, peso=?, categoria=?, data_da_captura=? where codigo=?";
        try (PreparedStatement ps = ConnectionFactory.getConnection().prepareStatement(sql)) {
            ps.setString(1, pokemon.getNome());
            ps.setDouble(2, pokemon.getAltura());
            ps.setDouble(3, pokemon.getPeso());
            ps.setString(4, pokemon.getCategoria());
            ps.setDate(5, Date.valueOf(pokemon.getDataDaCaptura()));
            ps.setLong(6, pokemon.getCodigo());
            // executeUpdate devolve quantas linhas foram alteradas: 0 = código não existe
            if (ps.executeUpdate() > 0) {
                return pokemon;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return null;
    }

    // DELETE /pokemon/{codigo} (aula 42): apaga um Pokémon e retorna true/false
    public boolean delete(Long codigo) {
        String sql = "delete from ddd_pokemons where codigo = ?";
        try (PreparedStatement ps = ConnectionFactory.getConnection().prepareStatement(sql)) {
            ps.setLong(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir: " + e.getMessage());
        } finally {
            ConnectionFactory.closeConnection();
        }
        return false;
    }
}
