package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.NoticiaDao;
import com.exemplo.noticias.model.Noticia;
import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Cadastro de noticias da empresa do usuario logado.
 * O empresa_id vem SEMPRE da sessao, nunca do formulario.
 */
@WebServlet("/app/noticias")
public class NoticiaServlet extends BaseServlet {

    private static final int MAX_TITULO = 150;
    private static final int MAX_TEXTO = 5000;

    private final NoticiaDao dao = new NoticiaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado u = usuario(req);

        Integer editar = inteiro(req.getParameter("editar"));
        if (editar != null) {
            Noticia noticia = dao.buscar(editar, u.getEmpresaId());
            if (noticia == null) {
                mensagem(req, "Notícia não encontrada.");
                redirecionar(req, resp, "/app/noticias");
                return;
            }
            req.setAttribute("noticia", noticia);
        }
        listar(req, resp, u);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado u = usuario(req);
        String acao = texto(req.getParameter("acao"));

        if ("excluir".equals(acao)) {
            Integer id = inteiro(req.getParameter("id"));
            boolean excluida = id != null && dao.excluir(id, u.getEmpresaId());
            mensagem(req, excluida ? "Notícia excluída." : "Notícia não encontrada.");
            redirecionar(req, resp, "/app/noticias");
            return;
        }

        Noticia noticia = new Noticia();
        Integer id = inteiro(req.getParameter("id"));
        noticia.setId(id == null ? 0 : id);
        noticia.setEmpresaId(u.getEmpresaId());
        noticia.setAutor(u.getNome());
        noticia.setTitulo(texto(req.getParameter("titulo")));
        noticia.setTexto(texto(req.getParameter("texto")));

        String erro = validar(noticia);
        if (erro != null) {
            req.setAttribute("erro", erro);
            req.setAttribute("noticia", noticia);
            listar(req, resp, u);
            return;
        }

        if (noticia.getId() > 0) {
            boolean atualizada = dao.atualizar(noticia);
            mensagem(req, atualizada ? "Notícia atualizada." : "Notícia não encontrada.");
        } else {
            dao.inserir(noticia);
            mensagem(req, "Notícia publicada.");
        }
        redirecionar(req, resp, "/app/noticias");
    }

    private String validar(Noticia n) {
        if (n.getTitulo().isEmpty()) {
            return "Informe o título.";
        }
        if (n.getTitulo().length() > MAX_TITULO) {
            return "O título pode ter no máximo " + MAX_TITULO + " caracteres.";
        }
        if (n.getTexto().isEmpty()) {
            return "Informe o texto da notícia.";
        }
        if (n.getTexto().length() > MAX_TEXTO) {
            return "O texto pode ter no máximo " + MAX_TEXTO + " caracteres.";
        }
        return null;
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp, UsuarioLogado u)
            throws ServletException, IOException {
        req.setAttribute("noticias", dao.listarDaEmpresa(u.getEmpresaId()));
        exibir(req, resp, "noticias.jsp");
    }
}
