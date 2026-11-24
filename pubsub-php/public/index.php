<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>PubSub</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/sakura.css/css/sakura.css" type="text/css">
</head>
<body>

<p id="aviso" hidden></p>

<h2>Destino</h2>
<p>
    <select id="destino">
        <option value="topico">Todos recebem</option>
        <option value="fila">Só um recebe (fila)</option>
    </select>
</p>

<h2>Assinantes</h2>
<p>
    Quantos: <input id="instancias" type="number" min="1" max="10" value="5">
    Mensagens que cada um lê: <input id="alvo" type="number" min="1" max="20" value="1">
    <label><input id="duravel" type="checkbox"> assinatura persistente</label>
</p>
<p>
    <button id="iniciar" type="button">Iniciar assinantes</button>
    <button id="parar" type="button">Parar todos</button>
</p>
<div id="assinantes"></div>

<h2>Publicador</h2>
<p>
    Mensagens: <input id="quantidade" type="number" min="1" max="20" value="5">
    Intervalo (s): <input id="intervalo" type="number" min="0" max="600" value="60">
    <label><input id="persistente" type="checkbox" checked> persistente</label>
</p>
<p>
    <button id="publicar" type="button">Publicar</button>
    <button id="cancelar" type="button">Cancelar</button>
</p>
<p id="status">Parado.</p>

<script src="app.js"></script>
</body>
</html>
