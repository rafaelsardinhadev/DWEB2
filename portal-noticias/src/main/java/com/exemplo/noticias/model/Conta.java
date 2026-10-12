package com.exemplo.noticias.model;

import com.exemplo.noticias.util.Formatos;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Conta a pagar ou a receber de uma empresa. */
public class Conta {

    public static final String PAGAR = "PAGAR";
    public static final String RECEBER = "RECEBER";

    private int id;
    private int empresaId;
    private String descricao;
    private BigDecimal valor;
    private LocalDate vencimento;
    private String tipo = PAGAR;
    private boolean paga;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(int empresaId) {
        this.empresaId = empresaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isPaga() {
        return paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }

    public String getTipoRotulo() {
        return PAGAR.equals(tipo) ? "A pagar" : "A receber";
    }

    public String getSituacaoRotulo() {
        if (paga) {
            return PAGAR.equals(tipo) ? "Paga" : "Recebida";
        }
        return "Em aberto";
    }

    public String getValorFormatado() {
        return valor == null ? "" : Formatos.moeda(valor);
    }

    /** Valor no formato aceito por input type="number" (ponto decimal). */
    public String getValorInput() {
        return valor == null ? "" : valor.toPlainString();
    }

    public String getVencimentoFormatado() {
        return Formatos.data(vencimento);
    }

    /** Data no formato aceito por input type="date" (yyyy-MM-dd). */
    public String getVencimentoInput() {
        return vencimento == null ? "" : vencimento.toString();
    }

    public boolean isVencida() {
        return !paga && vencimento != null && vencimento.isBefore(LocalDate.now());
    }
}
