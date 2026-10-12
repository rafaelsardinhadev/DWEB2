package com.exemplo.noticias.util;

import java.sql.SQLException;

/** Erro de acesso ao banco (evita ter que declarar SQLException em todo lugar). */
public class ErroBanco extends RuntimeException {

    public ErroBanco(SQLException causa) {
        super("Erro ao acessar o banco de dados", causa);
    }
}
