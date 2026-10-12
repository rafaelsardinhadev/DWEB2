package com.exemplo.noticias.model;

public enum TipoUsuario {
    COMUM("Comum"),
    ADMIN("Administrador");

    private final String rotulo;

    TipoUsuario(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
