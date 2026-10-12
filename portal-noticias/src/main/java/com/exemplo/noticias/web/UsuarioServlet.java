package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.EmpresaDao;
import com.exemplo.noticias.dao.UsuarioDao;
import com.exemplo.noticias.model.Conflitos;
import com.exemplo.noticias.model.Empresa;
import com.exemplo.noticias.model.TipoUsuario;
import com.exemplo.noticias.model.Usuario;
import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Cadastro de usuarios, so para administradores (o filtro devolve 403 para usuarios comuns).
 *
 * O administrador so enxerga e altera usuarios das empresas que ele administra: as empresas
 * dele que nao conflitam entre si (veja Conflitos.administraveis). A lista e recalculada
 * a cada requisicao a partir do banco, nunca vem do formulario.
 */
@WebServlet("/app/usuarios")
public class UsuarioServlet extends BaseServlet {

    private static final Pattern LOGIN_VALIDO = Pattern.compile("[a-z0-9._-]{3,30}");
    private static final int SENHA_MINIMA = 6;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final EmpresaDao empresaDao = new EmpresaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        listar(req, resp, usuario(req), empresasAdministradas(usuario(req)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado admin = usuario(req);
        List<Empresa> administradas = empresasAdministradas(admin);

        if ("remover".equals(texto(req.getParameter("acao")))) {
            remover(req, resp, admin, administradas);
            return;
        }
        criar(req, resp, admin, administradas);
    }

    private void criar(HttpServletRequest req, HttpServletResponse resp, UsuarioLogado admin,
                       List<Empresa> administradas) throws ServletException, IOException {

        Usuario novo = new Usuario();
        novo.setNome(texto(req.getParameter("nome")));
        novo.setLogin(texto(req.getParameter("login")).toLowerCase());
        String senha = req.getParameter("senha") == null ? "" : req.getParameter("senha");
        String confirmacao = req.getParameter("confirmacao") == null ? "" : req.getParameter("confirmacao");
        try {
            novo.setTipo(TipoUsuario.valueOf(texto(req.getParameter("tipo"))));
        } catch (IllegalArgumentException e) {
            novo.setTipo(null);
        }

        // Empresas marcadas: so valem as que o administrador realmente administra.
        Set<Integer> marcadas = new HashSet<>();
        String[] parametros = req.getParameterValues("empresas");
        if (parametros != null) {
            for (String p : parametros) {
                Integer id = inteiro(p);
                if (id != null) {
                    marcadas.add(id);
                }
            }
        }
        List<Empresa> escolhidas = new ArrayList<>();
        for (Empresa e : administradas) {
            if (marcadas.contains(e.getId())) {
                escolhidas.add(e);
            }
        }

        String erro = validar(novo, senha, confirmacao, marcadas, escolhidas);
        if (erro != null) {
            req.setAttribute("erro", erro);
            req.setAttribute("novo", novo);
            req.setAttribute("empresasMarcadas", escolhidas.stream().map(Empresa::getId).collect(Collectors.toSet()));
            listar(req, resp, admin, administradas);
            return;
        }

        usuarioDao.criar(novo, senha, escolhidas.stream().map(Empresa::getId).toList());
        mensagem(req, "Usuário " + novo.getLogin() + " cadastrado.");
        redirecionar(req, resp, "/app/usuarios");
    }

    private String validar(Usuario novo, String senha, String confirmacao, Set<Integer> marcadas,
                           List<Empresa> escolhidas) {
        if (novo.getNome().length() < 2 || novo.getNome().length() > 100) {
            return "Informe o nome (de 2 a 100 caracteres).";
        }
        if (!LOGIN_VALIDO.matcher(novo.getLogin()).matches()) {
            return "O login deve ter de 3 a 30 caracteres: letras minúsculas, números, ponto, hífen ou sublinhado.";
        }
        if (senha.length() < SENHA_MINIMA) {
            return "A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres.";
        }
        if (!senha.equals(confirmacao)) {
            return "A confirmação da senha não confere.";
        }
        if (novo.getTipo() == null) {
            return "Selecione o tipo do usuário.";
        }
        if (escolhidas.isEmpty()) {
            return "Selecione ao menos uma empresa.";
        }
        if (escolhidas.size() != marcadas.size()) {
            return "Você só pode vincular usuários às empresas que administra.";
        }
        if (Conflitos.temConflito(escolhidas)) {
            return "Empresas com conflito de interesses não podem ser vinculadas ao mesmo usuário.";
        }
        if (usuarioDao.loginExiste(novo.getLogin())) {
            return "Já existe um usuário com este login.";
        }
        return null;
    }

    private void remover(HttpServletRequest req, HttpServletResponse resp, UsuarioLogado admin,
                         List<Empresa> administradas) throws IOException {
        Integer id = inteiro(req.getParameter("id"));
        if (id == null) {
            mensagem(req, "Usuário não encontrado.");
        } else if (id == admin.getId()) {
            mensagem(req, "Você não pode remover o seu próprio usuário.");
        } else {
            usuarioDao.removerDasEmpresas(id, administradas.stream().map(Empresa::getId).toList());
            mensagem(req, "Usuário removido das empresas que você administra.");
        }
        redirecionar(req, resp, "/app/usuarios");
    }

    private List<Empresa> empresasAdministradas(UsuarioLogado admin) {
        return Conflitos.administraveis(empresaDao.listarDoUsuario(admin.getId()), admin.getEmpresaId());
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp, UsuarioLogado admin,
                        List<Empresa> administradas) throws ServletException, IOException {
        List<Integer> ids = administradas.stream().map(Empresa::getId).toList();
        if (req.getAttribute("empresasMarcadas") == null) {
            // formulario novo: ja vem marcada a empresa em que o administrador esta logado
            req.setAttribute("empresasMarcadas", Set.of(admin.getEmpresaId()));
        }
        req.setAttribute("empresasAdministradas", administradas);
        req.setAttribute("usuarios", usuarioDao.listarPorEmpresas(ids));
        req.setAttribute("tipos", TipoUsuario.values());
        exibir(req, resp, "usuarios.jsp");
    }
}
