package com.exemplo.noticias.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexoes JDBC com o banco H2 (arquivo na pasta do usuario).
 * Para usar outro banco (MySQL, PostgreSQL...) basta trocar URL/usuario/senha
 * e a dependencia no pom.xml; o resto do codigo e JDBC padrao.
 *
 * Pode ser configurado com -Ddweb.db.url, -Ddweb.db.user e -Ddweb.db.password.
 */
public final class Conexao {

    private static final String URL = System.getProperty("dweb.db.url",
            "jdbc:h2:file:~/dweb2-db/noticias;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
    private static final String USUARIO = System.getProperty("dweb.db.user", "sa");
    private static final String SENHA = System.getProperty("dweb.db.password", "dweb2");

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Conexao() {
    }

    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
