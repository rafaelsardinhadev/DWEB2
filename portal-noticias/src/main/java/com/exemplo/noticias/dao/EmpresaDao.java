package com.exemplo.noticias.dao;

import com.exemplo.noticias.model.Empresa;
import com.exemplo.noticias.util.Conexao;
import com.exemplo.noticias.util.ErroBanco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDao {

    public List<Empresa> listarTodas() {
        return consultar("SELECT id, nome, conflitos FROM empresa ORDER BY nome");
    }

    public Empresa buscar(int id) {
        String sql = "SELECT id, nome, conflitos FROM empresa WHERE id = ?";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    public List<Empresa> listarDoUsuario(int usuarioId) {
        String sql = "SELECT e.id, e.nome, e.conflitos FROM empresa e "
                + "JOIN usuario_empresa ue ON ue.empresa_id = e.id "
                + "WHERE ue.usuario_id = ? ORDER BY e.nome";
        return consultar(sql, usuarioId);
    }

    private List<Empresa> consultar(String sql, Object... parametros) {
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Empresa> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    private Empresa mapear(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getInt("id"));
        e.setNome(rs.getString("nome"));
        e.setConflitos(rs.getString("conflitos"));
        return e;
    }
}
