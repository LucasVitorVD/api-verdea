package com.verdea.api_verdea.config;

import com.verdea.api_verdea.services.authentication.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {
    private final CookieService cookieService;

    public JwtCookieFilter(CookieService cookieService) {
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Ignora rotas públicas
        if (path.startsWith("/api/auth/") || path.equals("/api/csrf")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token do cookie
        String token = cookieService.extractAccessTokenFromCookies(request);

        // Se o token estiver presente no cookie, adiciona-o ao cabeçalho Authorization
        // para que o filtro OAuth2ResourceServer/JWT padrão do Spring Security possa processá-lo
        if (token != null && !token.isBlank()) {
            // Cria um novo wrapper de requisição para modificar os cabeçalhos
            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return "Bearer " + token;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList("Bearer " + token));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> names = Collections.list(super.getHeaderNames());
                    if (!names.contains("Authorization")) {
                        names.add("Authorization");
                    }
                    return Collections.enumeration(names);
                }
            };

            // Continua a cadeia com o wrapper
            filterChain.doFilter(requestWrapper, response);
        } else {
            // Sem token no cookie, continua normalmente
            filterChain.doFilter(request, response);
        }
    }
}