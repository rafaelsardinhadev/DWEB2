package com.exemplo.noticias.web;

import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Porteiro da aplicacao:
 *  - define UTF-8 em todas as requisicoes;
 *  - tudo sob /app/ exige usuario logado;
 *  - /app/usuarios exige usuario administrador (usuario comum recebe 403);
 *  - todo POST sob /app/ precisa carregar o token CSRF da sessao.
 */
@WebFilter("/*")
public class SegurancaFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");

        String caminho = req.getServletPath();
        if (!caminho.startsWith("/app/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sessao = req.getSession(false);
        UsuarioLogado usuario = sessao == null ? null : (UsuarioLogado) sessao.getAttribute(BaseServlet.SESSAO_USUARIO);
        if (usuario == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Paginas internas nao devem ficar em cache (voltar apos o logout nao pode mostrar dados).
        res.setHeader("Cache-Control", "no-store");

        if ("POST".equals(req.getMethod()) && !tokenCsrfValido(req, sessao)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Token de seguranca invalido");
            return;
        }

        if (caminho.startsWith("/app/usuarios") && !usuario.isAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Apenas administradores");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean tokenCsrfValido(HttpServletRequest req, HttpSession sessao) {
        Object esperado = sessao.getAttribute(BaseServlet.SESSAO_CSRF);
        String recebido = req.getParameter("csrf");
        if (esperado == null || recebido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                esperado.toString().getBytes(StandardCharsets.UTF_8),
                recebido.getBytes(StandardCharsets.UTF_8));
    }
}
