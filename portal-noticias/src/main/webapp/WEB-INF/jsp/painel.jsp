<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Painel"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Olá, <c:out value="${usuarioLogado.nome}"/></h1>
<p class="subtitulo">
    Você está trabalhando na empresa <strong><c:out value="${usuarioLogado.empresaNome}"/></strong>
    como <strong><c:out value="${usuarioLogado.tipo.rotulo}"/></strong>.
    Só é possível alterar dados desta empresa.
</p>

<div class="grade">
    <a class="cartao" href="${ctx}/app/noticias">
        <div class="numero">${totalNoticias}</div>
        <div class="rotulo">notícias da empresa &rarr; gerenciar</div>
    </a>
    <a class="cartao" href="${ctx}/app/contas">
        <div class="numero">${contasAbertas}</div>
        <div class="rotulo">contas em aberto &rarr; gerenciar</div>
    </a>
    <c:if test="${usuarioLogado.admin}">
        <a class="cartao" href="${ctx}/app/usuarios">
            <div class="numero">&#128100;</div>
            <div class="rotulo">cadastro de usuários &rarr; gerenciar</div>
        </a>
    </c:if>
</div>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
