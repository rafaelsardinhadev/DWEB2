package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.ContaDao;
import com.exemplo.noticias.dao.NoticiaDao;
import com.exemplo.noticias.model.Conta;
import com.exemplo.noticias.model.UsuarioLogado;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Tela inicial depois do login: resumo da empresa em que o usuario entrou. */
@WebServlet("/app/painel")
public class PainelServlet extends BaseServlet {

    private final NoticiaDao noticiaDao = new NoticiaDao();
    private final ContaDao contaDao = new ContaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado u = usuario(req);
        req.setAttribute("totalNoticias", noticiaDao.listarDaEmpresa(u.getEmpresaId()).size());
        req.setAttribute("contasAbertas",
                contaDao.listarDaEmpresa(u.getEmpresaId()).stream().filter((Conta c) -> !c.isPaga()).count());
        exibir(req, resp, "painel.jsp");
    }
}
