package com.exemplo.noticias.dao;

import com.exemplo.noticias.model.Empresa;
import com.exemplo.noticias.model.TipoUsuario;
import com.exemplo.noticias.model.Usuario;
import com.exemplo.noticias.util.Conexao;
import com.exemplo.noticias.util.ErroBanco;
import com.exemplo.noticias.util.Senha;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsuarioDao {

    /**
     * Valida empresa + usuario + senha. So autentica se o usuario realmente pertence
     * a empresa informada. Retorna null quando as credenciais nao conferem.
     */
    public Usuario autenticar(int empresaId, String login, String senha) {
        String sql = "SELECT u.id, u.nome, u.login, u.tipo, u.senha_hash FROM usuario u "
                + "JOIN usuario_empresa ue ON ue.usuario_id = u.id "
                + "WHERE LOWER(u.login) = LOWER(?) AND ue.empresa_id = ?";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setInt(2, empresaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && Senha.confere(senha, rs.getString("senha_hash"))) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    public boolean loginExiste(String login) {
        String sql = "SELECT 1 FROM usuario WHERE LOWER(login) = LOWER(?)";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    public void criar(Usuario usuario, String senha, Collection<Integer> empresaIds) {
        String sqlUsuario = "INSERT INTO usuario (nome, login, senha_hash, tipo) VALUES (?, ?, ?, ?)";
        String sqlVinculo = "INSERT INTO usuario_empresa (usuario_id, empresa_id) VALUES (?, ?)";
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try {
                int novoId;
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, usuario.getNome());
                    ps.setString(2, usuario.getLogin());
                    ps.setString(3, Senha.gerarHash(senha));
                    ps.setString(4, usuario.getTipo().name());
                    ps.executeUpdate();
                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        chaves.next();
                        novoId = chaves.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlVinculo)) {
                    for (int empresaId : empresaIds) {
                        ps.setInt(1, novoId);
                        ps.setInt(2, empresaId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                usuario.setId(novoId);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    /**
     * Usuarios vinculados a pelo menos uma das empresas informadas. Cada usuario vem apenas
     * com as empresas dessa lista (o administrador nao enxerga as demais empresas dele).
     */
    public List<Usuario> listarPorEmpresas(Collection<Integer> empresaIds) {
        if (empresaIds.isEmpty()) {
            return List.of();
        }
        String interrogacoes = empresaIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT u.id, u.nome, u.login, u.tipo, e.id AS empresa_id, e.nome AS empresa_nome "
                + "FROM usuario u "
                + "JOIN usuario_empresa ue ON ue.usuario_id = u.id "
                + "JOIN empresa e ON e.id = ue.empresa_id "
                + "WHERE ue.empresa_id IN (" + interrogacoes + ") "
                + "ORDER BY u.nome, e.nome";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (int id : empresaIds) {
                ps.setInt(i++, id);
            }
            Map<Integer, Usuario> porId = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = porId.computeIfAbsent(rs.getInt("id"), k -> {
                        try {
                            return mapear(rs);
                        } catch (SQLException e) {
                            throw new ErroBanco(e);
                        }
                    });
                    Empresa empresa = new Empresa();
                    empresa.setId(rs.getInt("empresa_id"));
                    empresa.setNome(rs.getString("empresa_nome"));
                    u.getEmpresas().add(empresa);
                }
            }
            return new ArrayList<>(porId.values());
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    /**
     * Retira o usuario das empresas informadas. Se ele nao pertencer a mais nenhuma empresa,
     * o cadastro dele e apagado. Vinculos com outras empresas nao sao tocados.
     */
    public void removerDasEmpresas(int usuarioId, Collection<Integer> empresaIds) {
        if (empresaIds.isEmpty()) {
            return;
        }
        String interrogacoes = empresaIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sqlVinculos = "DELETE FROM usuario_empresa WHERE usuario_id = ? AND empresa_id IN (" + interrogacoes + ")";
        String sqlOrfao = "DELETE FROM usuario WHERE id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM usuario_empresa WHERE usuario_id = ?)";
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlVinculos)) {
                    int i = 1;
                    ps.setInt(i++, usuarioId);
                    for (int id : empresaIds) {
                        ps.setInt(i++, id);
                    }
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlOrfao)) {
                    ps.setInt(1, usuarioId);
                    ps.setInt(2, usuarioId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setLogin(rs.getString("login"));
        u.setTipo(TipoUsuario.valueOf(rs.getString("tipo")));
        return u;
    }
}
