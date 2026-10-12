<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Contas"/>
<%@ include file="/WEB-INF/jspf/topo.jspf" %>

<h1>Contas de <c:out value="${usuarioLogado.empresaNome}"/></h1>
<p class="subtitulo">Contas a pagar e a receber da empresa.</p>

<c:if test="${not empty mensagem}">
    <p class="alerta sucesso"><c:out value="${mensagem}"/></p>
</c:if>
<c:if test="${not empty erro}">
    <p class="alerta erro"><c:out value="${erro}"/></p>
</c:if>

<div class="resumo">
    <div class="cartao"><div class="valor"><c:out value="${totalPagar}"/></div><div class="rotulo">a pagar (em aberto)</div></div>
    <div class="cartao"><div class="valor"><c:out value="${totalReceber}"/></div><div class="rotulo">a receber (em aberto)</div></div>
    <div class="cartao"><div class="valor"><c:out value="${saldo}"/></div><div class="rotulo">saldo previsto</div></div>
</div>

<form method="post" action="${ctx}/app/contas" class="cartao" id="formulario">
    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
    <input type="hidden" name="acao" value="salvar">
    <c:if test="${conta.id > 0}">
        <input type="hidden" name="id" value="${conta.id}">
    </c:if>

    <h2 style="margin-top:0">${conta.id > 0 ? 'Editar conta' : 'Nova conta'}</h2>

    <div class="campo">
        <label for="descricao">Descrição</label>
        <input type="text" id="descricao" name="descricao" value="<c:out value='${conta.descricao}'/>" maxlength="120" required>
    </div>

    <div class="linha">
        <div class="campo">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo">
                <option value="PAGAR" ${conta.tipo ne 'RECEBER' ? 'selected' : ''}>A pagar</option>
                <option value="RECEBER" ${conta.tipo eq 'RECEBER' ? 'selected' : ''}>A receber</option>
            </select>
        </div>
        <div class="campo">
            <label for="valor">Valor (R$)</label>
            <input type="number" id="valor" name="valor" value="<c:out value='${conta.valorInput}'/>"
                   step="0.01" min="0.01" required>
        </div>
        <div class="campo">
            <label for="vencimento">Vencimento</label>
            <input type="date" id="vencimento" name="vencimento" value="<c:out value='${conta.vencimentoInput}'/>" required>
        </div>
    </div>

    <button type="submit" class="botao">${conta.id > 0 ? 'Salvar alterações' : 'Cadastrar'}</button>
    <c:if test="${conta.id > 0}">
        <a class="botao-secundario" href="${ctx}/app/contas">Cancelar</a>
    </c:if>
</form>

<h2>Contas cadastradas</h2>

<c:choose>
    <c:when test="${empty contas}">
        <p class="vazio">Nenhuma conta cadastrada.</p>
    </c:when>
    <c:otherwise>
        <div class="tabela-rolagem">
            <table class="tabela">
                <thead>
                <tr>
                    <th>Descrição</th>
                    <th>Tipo</th>
                    <th>Vencimento</th>
                    <th class="num">Valor</th>
                    <th>Situação</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="c" items="${contas}">
                    <tr class="${c.paga ? 'paga' : ''}">
                        <td class="descricao"><c:out value="${c.descricao}"/></td>
                        <td>
                            <span class="etiqueta ${c.tipo eq 'PAGAR' ? 'pagar' : 'receber'}"><c:out value="${c.tipoRotulo}"/></span>
                        </td>
                        <td><c:out value="${c.vencimentoFormatado}"/></td>
                        <td class="num"><c:out value="${c.valorFormatado}"/></td>
                        <td>
                            <c:out value="${c.situacaoRotulo}"/>
                            <c:if test="${c.vencida}"><span class="etiqueta vencida">vencida</span></c:if>
                        </td>
                        <td>
                            <div class="acoes">
                                <form method="post" action="${ctx}/app/contas">
                                    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                                    <input type="hidden" name="acao" value="alternar">
                                    <input type="hidden" name="id" value="${c.id}">
                                    <button type="submit" class="botao-pequeno">
                                        ${c.paga ? 'Reabrir' : (c.tipo eq 'PAGAR' ? 'Marcar paga' : 'Marcar recebida')}
                                    </button>
                                </form>
                                <a class="botao-pequeno" href="${ctx}/app/contas?editar=${c.id}#formulario">Editar</a>
                                <form method="post" action="${ctx}/app/contas"
                                      onsubmit="return confirm('Excluir esta conta?');">
                                    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                                    <input type="hidden" name="acao" value="excluir">
                                    <input type="hidden" name="id" value="${c.id}">
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
