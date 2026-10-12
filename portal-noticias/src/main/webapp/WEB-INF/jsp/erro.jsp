<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="titulo" value="Erro"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<c:choose>
    <c:when test="${codigo == 403}">
        <h1>Acesso negado</h1>
        <p class="alerta erro">Você não tem permissão para acessar esta página.</p>
    </c:when>
    <c:when test="${codigo == 404}">
        <h1>Página não encontrada</h1>
        <p class="alerta aviso">O endereço solicitado não existe.</p>
    </c:when>
    <c:otherwise>
        <h1>Algo deu errado</h1>
        <p class="alerta erro">Ocorreu um erro inesperado. Tente novamente em instantes.</p>
    </c:otherwise>
</c:choose>

<p><a href="${ctx}/noticias">Voltar para as notícias</a></p>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
