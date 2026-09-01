package Java;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller do cadastro de animais.
 * GET  /animais            -> mostra o formulario + a lista
 * POST /animais            -> cadastra um animal
 * POST /animais?acao=excluir&id=N -> remove um animal
 */
@WebServlet("/animais")
public class AnimalServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/animais.jsp";

    private final AnimalRepositorio repositorio = AnimalRepositorio.getInstancia();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Pet> animais = repositorio.listar();
        request.setAttribute("animais", animais);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if ("excluir".equals(request.getParameter("acao"))) {
            excluir(request, response);
            return;
        }

        String nome = valorOuVazio(request.getParameter("nome"));
        String raca = valorOuVazio(request.getParameter("raca"));
        String tipo = valorOuVazio(request.getParameter("tipo"));

        String erro = validar(nome, raca, tipo);
        if (erro != null) {
            request.setAttribute("erro", erro);
            request.setAttribute("nome", nome);
            request.setAttribute("raca", raca);
            request.setAttribute("tipo", tipo);
            request.setAttribute("animais", repositorio.listar());
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        repositorio.salvar(criarPet(tipo, nome, raca));

        // Post/Redirect/Get: evita reenviar o formulario ao atualizar a pagina
        response.sendRedirect(request.getContextPath() + "/animais?ok=1");
    }

    private void excluir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            repositorio.remover(Integer.parseInt(request.getParameter("id")));
        } catch (NumberFormatException ignorado) {
            // id invalido: apenas volta para a listagem
        }
        response.sendRedirect(request.getContextPath() + "/animais");
    }

    private Pet criarPet(String tipo, String nome, String raca) {
        return switch (tipo) {
            case "Gato" -> new Gato(nome, raca);
            case "Coelho" -> new Coelho(nome, raca);
            default -> new Cachorro(nome, raca);
        };
    }

    private String validar(String nome, String raca, String tipo) {
        if (nome.isEmpty()) {
            return "Informe o nome do animal.";
        }
        if (raca.isEmpty()) {
            return "Informe a raça do animal.";
        }
        if (!tipo.equals("Cachorro") && !tipo.equals("Gato") && !tipo.equals("Coelho")) {
            return "Selecione um tipo válido.";
        }
        return null;
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
