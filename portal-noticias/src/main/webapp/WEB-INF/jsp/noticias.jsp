<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Minhas notícias"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Notícias de <c:out value="${usuarioLogado.empresaNome}"/></h1>
<p class="subtitulo">As notícias cadastradas aparecem na <a href="${ctx}/noticias">página pública</a>.</p>

<c:if test="${not empty mensagem}">
    <p class="alerta sucesso"><c:out value="${mensagem}"/></p>
</c:if>
<c:if test="${not empty erro}">
    <p class="alerta erro"><c:out value="${erro}"/></p>
</c:if>

<form method="post" action="${ctx}/app/noticias" class="cartao" id="formulario">
    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
    <input type="hidden" name="acao" value="salvar">
    <c:if test="${noticia.id > 0}">
        <input type="hidden" name="id" value="${noticia.id}">
    </c:if>

    <h2 style="margin-top:0">${noticia.id > 0 ? 'Editar notícia' : 'Nova notícia'}</h2>

    <div class="campo">
        <label for="titulo">Título</label>
        <input type="text" id="titulo" name="titulo" value="<c:out value='${noticia.titulo}'/>" maxlength="150" required>
    </div>

    <div class="campo">
        <label for="texto">Texto</label>
        <textarea id="texto" name="texto" maxlength="5000" required><c:out value="${noticia.texto}"/></textarea>
    </div>

    <button type="submit" class="botao">${noticia.id > 0 ? 'Salvar alterações' : 'Publicar'}</button>
    <c:if test="${noticia.id > 0}">
        <a class="botao-secundario" href="${ctx}/app/noticias">Cancelar</a>
    </c:if>
</form>

<h2>Notícias publicadas</h2>

<c:choose>
    <c:when test="${empty noticias}">
        <p class="vazio">Sua empresa ainda não publicou notícias.</p>
    </c:when>
    <c:otherwise>
        <div class="tabela-rolagem">
            <table class="tabela">
                <thead>
                <tr>
                    <th>Título</th>
                    <th>Autor</th>
                    <th>Publicada em</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="n" items="${noticias}">
                    <tr>
                        <td><c:out value="${n.titulo}"/></td>
                        <td><c:out value="${n.autor}"/></td>
                        <td><c:out value="${n.dataFormatada}"/></td>
                        <td>
                            <div class="acoes">
                                <a class="botao-pequeno" href="${ctx}/app/noticias?editar=${n.id}#formulario">Editar</a>
                                <form method="post" action="${ctx}/app/noticias"
                                      onsubmit="return confirm('Excluir esta notícia?');">
                                    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                                    <input type="hidden" name="acao" value="excluir">
                                    <input type="hidden" name="id" value="${n.id}">
                                    <button type="submit" class="botao-excluir">Excluir</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
