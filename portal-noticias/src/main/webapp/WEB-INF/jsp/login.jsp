<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Entrar"/>
<c:set var="larguraEstreita" value="${true}"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Entrar</h1>
<p class="subtitulo">Informe a empresa, o usuário e a senha.</p>

<c:if test="${not empty erro}">
    <p class="alerta erro"><c:out value="${erro}"/></p>
</c:if>

<form method="post" action="${ctx}/login" class="cartao" autocomplete="off">
    <div class="campo">
        <label for="empresa">Empresa</label>
        <select id="empresa" name="empresa" required>
            <option value="">Selecione...</option>
            <c:forEach var="e" items="${empresas}">
                <option value="${e.id}" ${empresaSelecionada == e.id ? 'selected' : ''}><c:out value="${e.nome}"/></option>
            </c:forEach>
        </select>
    </div>

    <div class="campo">
        <label for="usuario">Usuário</label>
        <input type="text" id="usuario" name="usuario" value="<c:out value='${usuarioDigitado}'/>"
               maxlength="30" autocapitalize="none" required>
    </div>

    <div class="campo">
        <label for="senha">Senha</label>
        <input type="password" id="senha" name="senha" maxlength="100" autocomplete="current-password" required>
    </div>

    <button type="submit" class="botao">Entrar</button>
</form>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
