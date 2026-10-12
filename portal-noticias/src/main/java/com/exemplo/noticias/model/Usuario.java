package com.exemplo.noticias.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int id;
    private String nome;
    private String login;
    private TipoUsuario tipo = TipoUsuario.COMUM;
    /** Empresas do usuario (na listagem, apenas as que o administrador logado enxerga). */
    private List<Empresa> empresas = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public List<Empresa> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(List<Empresa> empresas) {
        this.empresas = empresas;
    }
}
