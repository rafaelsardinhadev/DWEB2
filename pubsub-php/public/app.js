const $ = (id) => document.getElementById(id);

async function enviar(acao, dados) {
    try {
        const resposta = await fetch('api.php?acao=' + acao, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(dados || {})
        });
        const json = await resposta.json().catch(() => ({}));
        aviso(resposta.ok ? '' : (json.erro || 'Erro ' + resposta.status));
    } catch (e) {
        aviso('Não foi possível falar com o servidor.');
    }
    atualizar();
}

function aviso(texto) {
    $('aviso').hidden = !texto;
    $('aviso').textContent = texto;
}

// ---------- Botões ----------

$('iniciar').addEventListener('click', () => enviar('iniciar-assinantes', {
    destino: $('destino').value,
    instancias: $('instancias').value,
    alvo: $('alvo').value,
    duravel: $('duravel').checked
}));

$('parar').addEventListener('click', () => enviar('parar-assinantes'));

$('publicar').addEventListener('click', () => enviar('publicar', {
    destino: $('destino').value,
    quantidade: $('quantidade').value,
    intervalo: $('intervalo').value,
    persistente: $('persistente').checked
}));

$('cancelar').addEventListener('click', () => enviar('cancelar'));

$('assinantes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button');
    if (botao) {
        enviar(botao.dataset.acao, { id: botao.dataset.id });
    }
});

const nomeDoEstado = {
    DESCONECTADO: 'desconectado', CONECTANDO: 'conectando', CONECTADO: 'conectado',
    CONCLUIDO: 'concluído', ERRO: 'erro'
};

function desenharAssinante(a) {
    const bloco = document.createElement('div');
    bloco.className = 'assinante';

    let titulo = 'Assinante ' + a.id + ' — ' + nomeDoEstado[a.estado] + ' — leu ' + a.recebidas.length + '/' + a.alvo;
    if (a.erro) {
        titulo += ' — ' + a.erro;
    }
    bloco.append(titulo + ' ');

    if (a.estado !== 'CONCLUIDO') {
        const botao = document.createElement('button');
        botao.type = 'button';
        botao.dataset.id = a.id;
        const ligado = a.estado === 'CONECTADO' || a.estado === 'CONECTANDO';
        botao.dataset.acao = ligado ? 'desconectar' : 'conectar';
        botao.textContent = ligado ? 'Desconectar' : 'Conectar';
        bloco.appendChild(botao);
    }

    const lista = document.createElement('ul');
    a.recebidas.forEach((m) => {
        const item = document.createElement('li');
        item.textContent = m.hora + ' — ' + m.texto;
        lista.appendChild(item);
    });
    bloco.appendChild(lista);
    return bloco;
}

function textoDoPublicador(p) {
    switch (p.estado) {
        case 'PUBLICANDO': return 'Publicando: ' + p.enviadas + '/' + p.total + ' enviadas.';
        case 'CONCLUIDO': return 'Concluído: ' + p.enviadas + ' mensagens enviadas.';
        case 'CANCELADO': return 'Cancelado depois de ' + p.enviadas + ' mensagem(ns).';
        case 'ERRO': return 'Erro: ' + p.erro;
        default: return 'Parado.';
    }
}

let semConexao = false;
let ultimaLista = '';

async function atualizar() {
    try {
        const resposta = await fetch('api.php?acao=estado', { cache: 'no-store' });
        const estado = await resposta.json();
        if (semConexao) {
            semConexao = false;
            aviso('');
        }

        $('status').textContent = textoDoPublicador(estado.publicador);
        $('publicar').disabled = estado.publicador.estado === 'PUBLICANDO';
        $('cancelar').disabled = estado.publicador.estado !== 'PUBLICANDO';

        const assinatura = JSON.stringify(estado.assinantes);
        if (assinatura !== ultimaLista) {
            ultimaLista = assinatura;
            $('assinantes').replaceChildren(...estado.assinantes.map(desenharAssinante));
        }
    } catch (e) {
        semConexao = true;
        aviso('Sem conexão com o servidor web (o servidor PHP está rodando?).');
    }
}

atualizar();
setInterval(atualizar, 1000);
