<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Usuários"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Usuários</h1>
<p class="subtitulo">
    Empresas que você administra nesta sessão:
    <c:forEach var="e" items="${empresasAdministradas}" varStatus="s">
        <span class="etiqueta"><c:out value="${e.nome}"/></span>
    </c:forEach>
</p>

<c:if test="${not empty mensagem}">
    <p class="alerta sucesso"><c:out value="${mensagem}"/></p>
</c:if>
<c:if test="${not empty erro}">
    <p class="alerta erro"><c:out value="${erro}"/></p>
</c:if>

<form method="post" action="${ctx}/app/usuarios" class="cartao" autocomplete="off">
    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
    <input type="hidden" name="acao" value="criar">

    <h2 style="margin-top:0">Novo usuário</h2>

    <div class="linha">
        <div class="campo">
            <label for="nome">Nome</label>
            <input type="text" id="nome" name="nome" value="<c:out value='${novo.nome}'/>" maxlength="100" required>
        </div>
        <div class="campo">
            <label for="login">Login</label>
            <input type="text" id="login" name="login" value="<c:out value='${novo.login}'/>"
                   maxlength="30" autocapitalize="none" required>
            <div class="dica">Letras minúsculas, números, ponto, hífen ou sublinhado.</div>
        </div>
    </div>

    <div class="linha">
        <div class="campo">
            <label for="senha">Senha</label>
            <input type="password" id="senha" name="senha" maxlength="100" autocomplete="new-password" required>
            <div class="dica">Mínimo de 6 caracteres.</div>
        </div>
        <div class="campo">
            <label for="confirmacao">Confirmar senha</label>
            <input type="password" id="confirmacao" name="confirmacao" maxlength="100" autocomplete="new-password" required>
        </div>
    </div>

    <div class="linha">
        <div class="campo">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo">
                <c:forEach var="t" items="${tipos}">
                    <option value="${t}" ${novo.tipo == t ? 'selected' : ''}><c:out value="${t.rotulo}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="campo">
            <label>Empresas</label>
            <div class="marcas">
                <c:forEach var="e" items="${empresasAdministradas}">
                    <label>
                        <input type="checkbox" name="empresas" value="${e.id}"
                               ${empresasMarcadas.contains(e.id) ? 'checked' : ''}>
                        <c:out value="${e.nome}"/>
                    </label>
                </c:forEach>
            </div>
        </div>
    </div>

    <button type="submit" class="botao">Cadastrar usuário</button>
</form>

<h2>Usuários das suas empresas</h2>

<c:choose>
    <c:when test="${empty usuarios}">
        <p class="vazio">Nenhum usuário encontrado.</p>
    </c:when>
    <c:otherwise>
        <div class="tabela-rolagem">
            <table class="tabela">
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>Login</th>
                    <th>Tipo</th>
                    <th>Empresas</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${usuarios}">
                    <tr>
                        <td><c:out value="${u.nome}"/></td>
                        <td><c:out value="${u.login}"/></td>
                        <td>
                            <span class="etiqueta ${u.tipo == 'ADMIN' ? 'admin' : ''}"><c:out value="${u.tipo.rotulo}"/></span>
                        </td>
                        <td>
                            <c:forEach var="e" items="${u.empresas}">
                                <span class="etiqueta"><c:out value="${e.nome}"/></span>
                            </c:forEach>
                        </td>
                        <td>
                            <c:if test="${u.id != usuarioLogado.id}">
                                <form method="post" action="${ctx}/app/usuarios"
                                      onsubmit="return confirm('Remover este usuário das suas empresas?');">
                                    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                                    <input type="hidden" name="acao" value="remover">
                                    <input type="hidden" name="id" value="${u.id}">
                                    <button type="submit" class="botao-excluir">Remover</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
