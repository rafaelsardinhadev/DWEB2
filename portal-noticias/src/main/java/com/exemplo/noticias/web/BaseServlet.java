package com.exemplo.noticias.web;

import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Utilitarios comuns aos servlets (sessao, mensagens, encaminhamento para JSP). */
public abstract class BaseServlet extends HttpServlet {

    public static final String SESSAO_USUARIO = "usuarioLogado";
    public static final String SESSAO_CSRF = "csrf";
    private static final String SESSAO_MENSAGEM = "mensagemFlash";

    /** Usuario da sessao (nunca nulo nas rotas /app/*, o filtro garante). */
    protected UsuarioLogado usuario(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        return sessao == null ? null : (UsuarioLogado) sessao.getAttribute(SESSAO_USUARIO);
    }

    /** Mensagem que sera exibida uma unica vez na proxima pagina (apos um redirect). */
    protected void mensagem(HttpServletRequest req, String texto) {
        req.getSession().setAttribute(SESSAO_MENSAGEM, texto);
    }

    protected void exibir(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        HttpSession sessao = req.getSession(false);
        if (sessao != null) {
            req.setAttribute("usuarioLogado", sessao.getAttribute(SESSAO_USUARIO));
            req.setAttribute("csrf", sessao.getAttribute(SESSAO_CSRF));
            Object mensagem = sessao.getAttribute(SESSAO_MENSAGEM);
            if (mensagem != null) {
                req.setAttribute("mensagem", mensagem);
                sessao.removeAttribute(SESSAO_MENSAGEM);
            }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/" + jsp).forward(req, resp);
    }

    protected void redirecionar(HttpServletRequest req, HttpServletResponse resp, String caminho) throws IOException {
        resp.sendRedirect(req.getContextPath() + caminho);
    }

    protected static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    protected static Integer inteiro(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
