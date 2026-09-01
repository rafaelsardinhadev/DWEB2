<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cadastro de Animais</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilo.css">
</head>
<body>

<div class="container">

    <h1>Cadastro de Animais</h1>

    <c:if test="${not empty erro}">
        <p class="alerta erro"><c:out value="${erro}"/></p>
    </c:if>

    <c:if test="${param.ok eq '1'}">
        <p class="alerta sucesso">Animal cadastrado com sucesso!</p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/animais" class="cartao">
        <div class="campo">
            <label for="nome">Nome</label>
            <input type="text" id="nome" name="nome" value="<c:out value='${nome}'/>" maxlength="60" required>
        </div>

        <div class="campo">
            <label for="raca">Raça</label>
            <input type="text" id="raca" name="raca" value="<c:out value='${raca}'/>" maxlength="60" required>
        </div>

        <div class="campo">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo">
                <option value="Cachorro" ${tipo eq 'Cachorro' ? 'selected' : ''}>Cachorro</option>
                <option value="Gato"     ${tipo eq 'Gato'     ? 'selected' : ''}>Gato</option>
                <option value="Coelho"   ${tipo eq 'Coelho'   ? 'selected' : ''}>Coelho</option>
            </select>
        </div>

        <button type="submit" class="botao">Cadastrar</button>
    </form>

    <h2>Animais cadastrados</h2>

    <c:choose>
        <c:when test="${empty animais}">
            <p class="vazio">Nenhum animal cadastrado até o momento.</p>
        </c:when>
        <c:otherwise>
            <table class="tabela">
                <thead>
                <tr>
                    <th>#</th>
                    <th>Nome</th>
                    <th>Raça</th>
                    <th>Tipo</th>
                    <th>Som</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="animal" items="${animais}">
                    <tr>
                        <td>${animal.id}</td>
                        <td><c:out value="${animal.nome}"/></td>
                        <td><c:out value="${animal.raca}"/></td>
                        <td><c:out value="${animal.tipo}"/></td>
                        <td><c:out value="${animal.som}"/></td>
                        <td>
                            <form method="post" action="${pageContext.request.contextPath}/animais"
                                  onsubmit="return confirm('Excluir este animal?');">
                                <input type="hidden" name="acao" value="excluir">
                                <input type="hidden" name="id" value="${animal.id}">
                                <button type="submit" class="botao-excluir">Excluir</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <p class="total">Total: ${animais.size()} animal(is).</p>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/">Voltar ao início</a></p>

</div>

</body>
</html>
