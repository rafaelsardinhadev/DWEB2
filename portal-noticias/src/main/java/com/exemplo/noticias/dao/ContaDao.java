package com.exemplo.noticias.dao;

import com.exemplo.noticias.model.Conta;
import com.exemplo.noticias.util.Conexao;
import com.exemplo.noticias.util.ErroBanco;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaDao {

    private static final String SELECT =
            "SELECT id, empresa_id, descricao, valor, vencimento, tipo, paga FROM conta ";

    public List<Conta> listarDaEmpresa(int empresaId) {
        return consultar(SELECT + "WHERE empresa_id = ? ORDER BY paga, vencimento, id", empresaId);
    }

    public Conta buscar(int id, int empresaId) {
        List<Conta> lista = consultar(SELECT + "WHERE id = ? AND empresa_id = ?", id, empresaId);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public void inserir(Conta c) {
        String sql = "INSERT INTO conta (empresa_id, descricao, valor, vencimento, tipo, paga) VALUES (?, ?, ?, ?, ?, FALSE)";
        executar(sql, c.getEmpresaId(), c.getDescricao(), c.getValor(), Date.valueOf(c.getVencimento()), c.getTipo());
    }

    public boolean atualizar(Conta c) {
        String sql = "UPDATE conta SET descricao = ?, valor = ?, vencimento = ?, tipo = ? WHERE id = ? AND empresa_id = ?";
        return executar(sql, c.getDescricao(), c.getValor(), Date.valueOf(c.getVencimento()), c.getTipo(),
                c.getId(), c.getEmpresaId()) > 0;
    }

    public boolean alternarPaga(int id, int empresaId) {
        return executar("UPDATE conta SET paga = NOT paga WHERE id = ? AND empresa_id = ?", id, empresaId) > 0;
    }

    public boolean excluir(int id, int empresaId) {
        return executar("DELETE FROM conta WHERE id = ? AND empresa_id = ?", id, empresaId) > 0;
    }

    private int executar(String sql, Object... parametros) {
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    private List<Conta> consultar(String sql, Object... parametros) {
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Conta> lista = new ArrayList<>();
                while (rs.next()) {
                    Conta c = new Conta();
                    c.setId(rs.getInt("id"));
                    c.setEmpresaId(rs.getInt("empresa_id"));
                    c.setDescricao(rs.getString("descricao"));
                    c.setValor(rs.getObject("valor", BigDecimal.class));
                    c.setVencimento(rs.getObject("vencimento", LocalDate.class));
                    c.setTipo(rs.getString("tipo"));
                    c.setPaga(rs.getBoolean("paga"));
                    lista.add(c);
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }
}
