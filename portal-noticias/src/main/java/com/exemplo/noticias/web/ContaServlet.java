package com.exemplo.noticias.web;

import com.exemplo.noticias.dao.ContaDao;
import com.exemplo.noticias.model.Conta;
import com.exemplo.noticias.model.UsuarioLogado;
import com.exemplo.noticias.util.Formatos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Contas a pagar / a receber da empresa do usuario logado.
 * O empresa_id vem SEMPRE da sessao, nunca do formulario.
 */
@WebServlet("/app/contas")
public class ContaServlet extends BaseServlet {

    private static final int MAX_DESCRICAO = 120;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("9999999999.99");

    private final ContaDao dao = new ContaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado u = usuario(req);

        Integer editar = inteiro(req.getParameter("editar"));
        if (editar != null) {
            Conta conta = dao.buscar(editar, u.getEmpresaId());
            if (conta == null) {
                mensagem(req, "Conta não encontrada.");
                redirecionar(req, resp, "/app/contas");
                return;
            }
            req.setAttribute("conta", conta);
        }
        listar(req, resp, u);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UsuarioLogado u = usuario(req);
        String acao = texto(req.getParameter("acao"));
        Integer id = inteiro(req.getParameter("id"));

        if ("excluir".equals(acao)) {
            boolean ok = id != null && dao.excluir(id, u.getEmpresaId());
            mensagem(req, ok ? "Conta excluída." : "Conta não encontrada.");
            redirecionar(req, resp, "/app/contas");
            return;
        }
        if ("alternar".equals(acao)) {
            boolean ok = id != null && dao.alternarPaga(id, u.getEmpresaId());
            mensagem(req, ok ? "Situação da conta atualizada." : "Conta não encontrada.");
            redirecionar(req, resp, "/app/contas");
            return;
        }

        Conta conta = new Conta();
        conta.setId(id == null ? 0 : id);
        conta.setEmpresaId(u.getEmpresaId());
        conta.setDescricao(texto(req.getParameter("descricao")));
        conta.setTipo(texto(req.getParameter("tipo")));

        String erro = preencherEValidar(conta, texto(req.getParameter("valor")), texto(req.getParameter("vencimento")));
        if (erro != null) {
            req.setAttribute("erro", erro);
            req.setAttribute("conta", conta);
            listar(req, resp, u);
            return;
        }

        if (conta.getId() > 0) {
            boolean ok = dao.atualizar(conta);
            mensagem(req, ok ? "Conta atualizada." : "Conta não encontrada.");
        } else {
            dao.inserir(conta);
            mensagem(req, "Conta cadastrada.");
        }
        redirecionar(req, resp, "/app/contas");
    }

    /** Converte valor e vencimento (texto do formulario) e valida a conta. Retorna a mensagem de erro, ou null. */
    private String preencherEValidar(Conta conta, String valorTexto, String vencimentoTexto) {
        if (conta.getDescricao().isEmpty()) {
            return "Informe a descrição da conta.";
        }
        if (conta.getDescricao().length() > MAX_DESCRICAO) {
            return "A descrição pode ter no máximo " + MAX_DESCRICAO + " caracteres.";
        }
        if (!Conta.PAGAR.equals(conta.getTipo()) && !Conta.RECEBER.equals(conta.getTipo())) {
            return "Selecione o tipo da conta.";
        }
        try {
            BigDecimal valor = new BigDecimal(valorTexto.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
            conta.setValor(valor);
            if (valor.signum() <= 0 || valor.compareTo(VALOR_MAXIMO) > 0) {
                return "Informe um valor maior que zero.";
            }
        } catch (NumberFormatException e) {
            return "Valor inválido.";
        }
        try {
            conta.setVencimento(LocalDate.parse(vencimentoTexto));
        } catch (DateTimeParseException e) {
            return "Informe a data de vencimento.";
        }
        return null;
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp, UsuarioLogado u)
            throws ServletException, IOException {
        List<Conta> contas = dao.listarDaEmpresa(u.getEmpresaId());

        BigDecimal aPagar = somaEmAberto(contas, Conta.PAGAR);
        BigDecimal aReceber = somaEmAberto(contas, Conta.RECEBER);

        req.setAttribute("contas", contas);
        req.setAttribute("totalPagar", Formatos.moeda(aPagar));
        req.setAttribute("totalReceber", Formatos.moeda(aReceber));
        req.setAttribute("saldo", Formatos.moeda(aReceber.subtract(aPagar)));
        exibir(req, resp, "contas.jsp");
    }

    private BigDecimal somaEmAberto(List<Conta> contas, String tipo) {
        return contas.stream()
                .filter(c -> !c.isPaga() && tipo.equals(c.getTipo()))
                .map(Conta::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
