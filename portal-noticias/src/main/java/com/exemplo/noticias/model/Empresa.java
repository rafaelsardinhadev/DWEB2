package com.exemplo.noticias.model;

import java.util.HashSet;
import java.util.Set;

public class Empresa {

    private int id;
    private String nome;
    /** IDs das empresas com conflito de interesses, separados por virgula (coluna empresa.conflitos). */
    private String conflitos = "";

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

    public String getConflitos() {
        return conflitos;
    }

    public void setConflitos(String conflitos) {
        this.conflitos = conflitos == null ? "" : conflitos;
    }

    /** Interpreta o campo "conflitos" como um conjunto de IDs (ignora lixo no texto). */
    public Set<Integer> getIdsConflito() {
        Set<Integer> ids = new HashSet<>();
        for (String parte : conflitos.split("[,;\\s]+")) {
            try {
                ids.add(Integer.parseInt(parte));
            } catch (NumberFormatException ignorado) {
                // trecho vazio ou invalido
            }
        }
        return ids;
    }
}
