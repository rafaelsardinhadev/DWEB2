package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.EmpresaDao;
import com.exemplo.noticias.dao.NoticiaDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Pagina publica: noticias de todas as empresas, com a empresa de origem. Nao exige login. */
@WebServlet("/noticias")
public class PortalServlet extends BaseServlet {

    private final NoticiaDao noticiaDao = new NoticiaDao();
    private final EmpresaDao empresaDao = new EmpresaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer filtro = inteiro(req.getParameter("empresa"));
        req.setAttribute("filtro", filtro);
        req.setAttribute("empresas", empresaDao.listarTodas());
        req.setAttribute("noticias", noticiaDao.listarPublicas(filtro));
        exibir(req, resp, "portal.jsp");
    }
}
