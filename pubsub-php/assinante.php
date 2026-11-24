<?php

declare(strict_types=1);

require __DIR__ . '/src/biblioteca.php';

use Stomp\Transport\Frame;

$p = lerArgumentos($argv);
$id = $p['id'] ?? '1';
$endereco = $p['endereco'] ?? ENDERECO_PADRAO;
$tipoDestino = $p['destino'] ?? 'topico';
$nomeDestino = $p['nome'] ?? 'lab.noticias';
$duravel = ($p['duravel'] ?? 'false') === 'true';
$quantidade = (int) ($p['quantidade'] ?? 1);
$espera = (int) ($p['espera'] ?? 0);
$soConectar = ($p['so-conectar'] ?? 'false') === 'true';
$apagarAssinatura = ($p['apagar-assinatura'] ?? 'false') === 'true';
$saida = $p['saida'] ?? null;
$arquivoParar = ($saida ?? 'dados/sem-saida') . '.parar';

if ($duravel && $tipoDestino === 'fila') {
    fwrite(STDERR, "--duravel so faz sentido com --destino=topico\n");
    exit(2);
}

$quem = 'assinante-' . $id;
$destino = caminhoDoDestino($tipoDestino, $nomeDestino);
$nomeDaAssinatura = 'assinatura-' . $id;

$recebidas = ($saida !== null && is_file($saida)) ? (json_decode((string) file_get_contents($saida), true)['recebidas'] ?? []) : [];
$estado = ['id' => (int) $id, 'estado' => 'CONECTANDO', 'erro' => '', 'destino' => $tipoDestino, 'nome' => $nomeDestino,
    'duravel' => $duravel, 'alvo' => $quantidade, 'recebidas' => $recebidas];
gravarEstado($saida, $estado);

try {
    $comClientId = $duravel || $apagarAssinatura;
    $stomp = conectar($endereco, $p['usuario'] ?? 'admin', $p['senha'] ?? 'admin', $comClientId ? 'cliente-' . $id : null);
    $stomp->getClient()->getConnection()->setReadTimeout(1);   // espera de 1 em 1 segundo por uma mensagem

    $cabecalhos = ['activemq.prefetchSize' => '1'];
    if ($comClientId) {
        $cabecalhos['activemq.subscriptionName'] = $nomeDaAssinatura;
    }
    $idDaInscricao = $stomp->subscribe($destino, null, 'client-individual', $cabecalhos);

    $tipo = $tipoDestino === 'fila' ? 'FILA' : ($duravel ? 'TOPICO-DURAVEL' : 'TOPICO-NAO-DURAVEL');
    registrar($quem, sprintf('conectado | destino=%s | tipo=%s | vai ler %d mensagem(ns)', $destino, $tipo, $quantidade));

    if ($apagarAssinatura) {
        removerAssinaturaDuravel($stomp, $destino, (string) $idDaInscricao, $nomeDaAssinatura);
        registrar($quem, 'assinatura duravel removida');
        exit(0);
    }

    $estado['estado'] = 'CONECTADO';
    gravarEstado($saida, $estado);

    if ($soConectar) {
        registrar($quem, 'assinatura duravel criada; saindo (o assinante fica OFFLINE)');
        exit(0);
    }

    $ultimaMensagem = time();
    while (count($recebidas) < $quantidade) {
        if (is_file($arquivoParar)) {
            registrar($quem, 'desconectado');
            break;
        }
        $frame = $stomp->read();
        if (!($frame instanceof Frame) || $frame->getCommand() !== 'MESSAGE') {
            gravarEstado($saida, $estado);   // mostra q o processo continua vivo
            if ($espera > 0 && time() - $ultimaMensagem >= $espera) {
                registrar($quem, 'tempo esgotado: nenhuma mensagem recebida');
                break;
            }
            continue;
        }
        $modo = ($frame['persistent'] ?? 'false') === 'true' ? 'PERSISTENTE' : 'NAO-PERSISTENTE';
        $recebidas[] = ['hora' => date('H:i:s'), 'texto' => $frame->getBody()];
        $stomp->ack($frame);
        $ultimaMensagem = time();
        registrar($quem, 'RECEBIDA <- ' . $frame->getBody() . ' [' . $modo . ']');
        $estado['recebidas'] = $recebidas;
        gravarEstado($saida, $estado);
    }

    $estado['estado'] = count($recebidas) >= $quantidade ? 'CONCLUIDO' : 'DESCONECTADO';
} catch (Throwable $e) {
    $estado['estado'] = 'ERRO';
    $estado['erro'] = 'não foi possível falar com o broker (' . $endereco . '): ' . $e->getMessage();
    registrar($quem, 'ERRO: ' . $estado['erro']);
}
gravarEstado($saida, $estado);
@unlink($arquivoParar);
exit($estado['estado'] === 'ERRO' ? 1 : 0);
