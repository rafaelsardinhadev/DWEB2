package com.exemplo.noticias.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Regras de conflito de interesses entre empresas.
 * O conflito e tratado como simetrico: se A lista B (ou B lista A), as duas conflitam.
 */
public final class Conflitos {

    private Conflitos() {
    }

    public static boolean conflitam(Empresa a, Empresa b) {
        if (a.getId() == b.getId()) {
            return false;
        }
        return a.getIdsConflito().contains(b.getId()) || b.getIdsConflito().contains(a.getId());
    }

    /** true se existe ao menos um par de empresas em conflito dentro do grupo. */
    public static boolean temConflito(Collection<Empresa> empresas) {
        List<Empresa> lista = new ArrayList<>(empresas);
        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                if (conflitam(lista.get(i), lista.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Empresas que um administrador pode administrar na sessao atual: parte da empresa em que
     * ele logou e so acrescenta as demais empresas dele que nao conflitam com nenhuma ja aceita.
     * Assim, mesmo que o banco tenha um administrador ligado a empresas rivais, ele nunca
     * administra duas rivais ao mesmo tempo.
     */
    public static List<Empresa> administraveis(List<Empresa> vinculadas, int empresaDaSessao) {
        Empresa base = vinculadas.stream().filter(e -> e.getId() == empresaDaSessao).findFirst().orElse(null);
        if (base == null) {
            return List.of();
        }
        List<Empresa> aceitas = new ArrayList<>();
        aceitas.add(base);
        for (Empresa candidata : vinculadas) {
            if (candidata != base && aceitas.stream().noneMatch(a -> conflitam(a, candidata))) {
                aceitas.add(candidata);
            }
        }
        aceitas.sort(Comparator.comparing(Empresa::getNome));
        return aceitas;
    }
}
