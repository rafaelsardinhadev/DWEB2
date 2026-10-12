package com.exemplo.noticias.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatacao de valores para exibicao (pt-BR). */
public final class Formatos {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private Formatos() {
    }

    public static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    public static String data(LocalDate data) {
        return data == null ? "" : DATA.format(data);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? "" : DATA_HORA.format(dataHora);
    }
}
