package br.com.fiap.resource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CORS (Cross-Origin Resource Sharing) - aula 44.
 *
 * O navegador bloqueia, por segurança, chamadas de uma página aberta em um endereço
 * para uma API em outra origem. Este filtro roda antes de toda requisição e adiciona
 * os cabeçalhos que dizem ao navegador quem pode acessar a API e com quais métodos.
 * (Clientes de terminal, como curl e Insomnia, não sofrem esse bloqueio; para ver o filtro
 * em ação pelo terminal: curl -i -X OPTIONS http://localhost:8080/pokemon)
 */
@Component
public class CorsFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        // o navegador envia um OPTIONS ("preflight") antes de POST/PUT/DELETE para perguntar
        // se pode fazer a chamada; respondemos 200 OK e não seguimos adiante
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
