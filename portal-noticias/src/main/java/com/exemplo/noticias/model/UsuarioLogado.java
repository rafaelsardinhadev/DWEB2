package com.exemplo.noticias.model;

import java.io.Serializable;

/** Dados guardados na sessao: quem e o usuario e em qual empresa ele entrou. */
public class UsuarioLogado implements Serializable {

    private final int id;
    private final String nome;
    private final String login;
    private final TipoUsuario tipo;
    private final int empresaId;
    private final String empresaNome;

    public UsuarioLogado(Usuario usuario, Empresa empresa) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.tipo = usuario.getTipo();
        this.empresaId = empresa.getId();
        this.empresaNome = empresa.getNome();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public String getEmpresaNome() {
        return empresaNome;
    }

    public boolean isAdmin() {
        return tipo == TipoUsuario.ADMIN;
    }
}
