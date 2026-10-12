package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.UsuarioDao;
import com.exemplo.noticias.model.TipoUsuario;
import com.exemplo.noticias.model.Usuario;
import com.exemplo.noticias.util.Conexao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Ao subir a aplicacao: cria as tabelas (schema.sql) e, se o banco estiver vazio,
 * carrega as empresas e dados de exemplo (dados.sql) e os usuarios de demonstracao.
 */
@WebListener
public class InicializadorBanco implements ServletContextListener {

    /** Senha dos usuarios de demonstracao (veja o README). */
    private static final String SENHA_DEMO = "senha123";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try (Connection con = Conexao.obter(); Statement st = con.createStatement()) {
            executarScript(st, "/schema.sql");

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM empresa")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    executarScript(st, "/dados.sql");
                    criarUsuariosDemonstracao();
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Falha ao inicializar o banco de dados", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        // Fecha o H2 de forma limpa para o arquivo nao ficar travado em um redeploy.
        try (Connection con = Conexao.obter(); Statement st = con.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (SQLException ignorado) {
            // banco ja encerrado
        }
    }

    private void criarUsuariosDemonstracao() {
        UsuarioDao dao = new UsuarioDao();
        // Ids das empresas: 1 Alfa, 2 Beta (rival da Alfa), 3 Gama, 4 Delta, 5 Epsilon (rival da Gama)
        criar(dao, "Administrador Geral", "admin", TipoUsuario.ADMIN, List.of(1, 3, 4));
        // Ligado a empresas rivais de proposito: demonstra a regra de conflito de interesses.
        criar(dao, "Chefe Multiempresa", "chefe", TipoUsuario.ADMIN, List.of(1, 2, 3));
        criar(dao, "Maria Souza", "maria", TipoUsuario.COMUM, List.of(1));
        criar(dao, "João Lima", "joao", TipoUsuario.COMUM, List.of(2));
        criar(dao, "Ana Costa", "ana", TipoUsuario.COMUM, List.of(3, 4));
    }

    private void criar(UsuarioDao dao, String nome, String login, TipoUsuario tipo, List<Integer> empresas) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setLogin(login);
        u.setTipo(tipo);
        dao.criar(u, SENHA_DEMO, empresas);
    }

    private void executarScript(Statement st, String recurso) throws IOException, SQLException {
        try (InputStream in = getClass().getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IOException("Recurso nao encontrado: " + recurso);
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?m)^\\s*--.*$", ""); // remove linhas de comentario
            for (String comando : script.split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando);
                }
            }
        }
    }
}
