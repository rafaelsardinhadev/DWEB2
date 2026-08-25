package Java;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/animais")
public class AnimalServlet extends HttpServlet {

    private static final List<Pet> animais = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tipo = request.getParameter("tipo");
        String nome = request.getParameter("nome");
        String raca = request.getParameter("raca");

        Pet pet = switch (tipo) {
            case "Gato" -> new Gato(raca, nome);
            case "Coelho" -> new Coelho(raca, nome);
            default -> new Cachorro(raca, nome);
        };
        animais.add(pet);

        response.sendRedirect("animais");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='pt-br'>");
        out.println("<head><meta charset='UTF-8'><title>Cadastro de Animais</title></head>");
        out.println("<body>");
        out.println("<h1>Cadastro de Animais</h1>");

        out.println("<form method='post' action='animais'>");
        out.println("Nome: <input type='text' name='nome' required><br><br>");
        out.println("Raça: <input type='text' name='raca' required><br><br>");
        out.println("Tipo: ");
        out.println("<select name='tipo'>");
        out.println("<option value='Cachorro'>Cachorro</option>");
        out.println("<option value='Gato'>Gato</option>");
        out.println("<option value='Coelho'>Coelho</option>");
        out.println("</select><br><br>");
        out.println("<input type='submit' value='Cadastrar'>");
        out.println("</form>");

        out.println("<h2>Animais cadastrados</h2>");
        out.println("<table border='1' cellpadding='5'>");
        out.println("<tr><th>Nome</th><th>Raça</th><th>Tipo</th><th>Som</th></tr>");
        for (Pet pet : animais) {
            out.println("<tr>");
            out.println("<td>" + escape(pet.getNome()) + "</td>");
            out.println("<td>" + escape(pet.getRaca()) + "</td>");
            out.println("<td>" + pet.getClass().getSimpleName() + "</td>");
            out.println("<td>" + pet.latir() + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");

        out.println("</body></html>");
    }

    private static String escape(String texto) {
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
