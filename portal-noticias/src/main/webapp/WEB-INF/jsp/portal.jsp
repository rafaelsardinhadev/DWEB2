<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Notícias"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Notícias</h1>
<p class="subtitulo">Publicações de todas as empresas participantes.</p>

<form method="get" action="${ctx}/noticias" class="filtro">
    <label for="empresa">Filtrar por empresa:</label>
    <select id="empresa" name="empresa" onchange="this.form.submit()">
        <option value="">Todas as empresas</option>
        <c:forEach var="e" items="${empresas}">
            <option value="${e.id}" ${filtro == e.id ? 'selected' : ''}><c:out value="${e.nome}"/></option>
        </c:forEach>
    </select>
    <noscript><button type="submit" class="botao-secundario">Filtrar</button></noscript>
</form>

<c:choose>
    <c:when test="${empty noticias}">
        <p class="vazio">Nenhuma notícia publicada até o momento.</p>
    </c:when>
    <c:otherwise>
        <c:forEach var="n" items="${noticias}">
            <article class="noticia">
                <span class="etiqueta"><c:out value="${n.empresaNome}"/></span>
                <h3><c:out value="${n.titulo}"/></h3>
                <p class="meta">Por <c:out value="${n.autor}"/> &middot; <c:out value="${n.dataFormatada}"/></p>
                <p class="corpo"><c:out value="${n.texto}"/></p>
            </article>
        </c:forEach>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
