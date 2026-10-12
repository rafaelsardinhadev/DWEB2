package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.EmpresaDao;
import com.exemplo.noticias.dao.UsuarioDao;
import com.exemplo.noticias.model.Empresa;
import com.exemplo.noticias.model.Usuario;
import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/** Login com tres campos: empresa, usuario e senha. */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final EmpresaDao empresaDao = new EmpresaDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (usuario(req) != null) {
            redirecionar(req, resp, "/app/painel");
            return;
        }
        mostrarFormulario(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer empresaId = inteiro(req.getParameter("empresa"));
        String login = texto(req.getParameter("usuario"));
        String senha = req.getParameter("senha") == null ? "" : req.getParameter("senha");

        Empresa empresa = empresaId == null ? null : empresaDao.buscar(empresaId);
        Usuario usuario = null;
        if (empresa != null && !login.isEmpty() && !senha.isEmpty()) {
            usuario = usuarioDao.autenticar(empresa.getId(), login, senha);
        }

        if (usuario == null) {
            // Mensagem unica: nao revela se o erro foi na empresa, no usuario ou na senha.
            req.setAttribute("erro", "Empresa, usuário ou senha inválidos.");
            req.setAttribute("empresaSelecionada", empresaId);
            req.setAttribute("usuarioDigitado", login);
            mostrarFormulario(req, resp);
            return;
        }

        // Nova sessao apos o login (evita fixacao de sessao).
        HttpSession antiga = req.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }
        HttpSession sessao = req.getSession(true);
        sessao.setAttribute(SESSAO_USUARIO, new UsuarioLogado(usuario, empresa));
        sessao.setAttribute(SESSAO_CSRF, gerarToken());
        redirecionar(req, resp, "/app/painel");
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("empresas", empresaDao.listarTodas());
        exibir(req, resp, "login.jsp");
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
