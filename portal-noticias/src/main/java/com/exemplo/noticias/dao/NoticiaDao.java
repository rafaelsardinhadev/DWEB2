package com.exemplo.noticias.dao;

import com.exemplo.noticias.model.Noticia;
import com.exemplo.noticias.util.Conexao;
import com.exemplo.noticias.util.ErroBanco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticiaDao {

    private static final String SELECT = "SELECT n.id, n.empresa_id, e.nome AS empresa_nome, n.autor, "
            + "n.titulo, n.texto, n.data_publicacao FROM noticia n JOIN empresa e ON e.id = n.empresa_id ";
    private static final String ORDEM = " ORDER BY n.data_publicacao DESC, n.id DESC";

    public List<Noticia> listarPublicas(Integer empresaId) {
        if (empresaId == null) {
            return consultar(SELECT + ORDEM);
        }
        return consultar(SELECT + "WHERE n.empresa_id = ?" + ORDEM, empresaId);
    }

    public List<Noticia> listarDaEmpresa(int empresaId) {
        return consultar(SELECT + "WHERE n.empresa_id = ?" + ORDEM, empresaId);
    }

    public Noticia buscar(int id, int empresaId) {
        List<Noticia> lista = consultar(SELECT + "WHERE n.id = ? AND n.empresa_id = ?", id, empresaId);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public void inserir(Noticia n) {
        String sql = "INSERT INTO noticia (empresa_id, autor, titulo, texto, data_publicacao) VALUES (?, ?, ?, ?, ?)";
        executar(sql, n.getEmpresaId(), n.getAutor(), n.getTitulo(), n.getTexto(), LocalDateTime.now());
    }

    public boolean atualizar(Noticia n) {
        String sql = "UPDATE noticia SET titulo = ?, texto = ? WHERE id = ? AND empresa_id = ?";
        return executar(sql, n.getTitulo(), n.getTexto(), n.getId(), n.getEmpresaId()) > 0;
    }

    public boolean excluir(int id, int empresaId) {
        return executar("DELETE FROM noticia WHERE id = ? AND empresa_id = ?", id, empresaId) > 0;
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

    private List<Noticia> consultar(String sql, Object... parametros) {
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Noticia> lista = new ArrayList<>();
                while (rs.next()) {
                    Noticia n = new Noticia();
                    n.setId(rs.getInt("id"));
                    n.setEmpresaId(rs.getInt("empresa_id"));
                    n.setEmpresaNome(rs.getString("empresa_nome"));
                    n.setAutor(rs.getString("autor"));
                    n.setTitulo(rs.getString("titulo"));
                    n.setTexto(rs.getString("texto"));
                    n.setDataPublicacao(rs.getObject("data_publicacao", LocalDateTime.class));
                    lista.add(n);
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new ErroBanco(e);
        }
    }
}
