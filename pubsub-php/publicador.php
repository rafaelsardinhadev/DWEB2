<?php
declare(strict_types=1);

require __DIR__ . '/src/biblioteca.php';

use Stomp\Transport\Message;

$p = lerArgumentos($argv);
$endereco = $p['endereco'] ?? ENDERECO_PADRAO;
$usuario = $p['usuario'] ?? 'admin';
$senha = $p['senha'] ?? 'admin';
$tipoDestino = $p['destino'] ?? 'topico';
$nomeDestino = $p['nome'] ?? 'lab.noticias';
$persistente = ($p['persistente'] ?? 'true') === 'true';
$quantidade = (int) ($p['quantidade'] ?? 5);
$intervalo = (int) ($p['intervalo'] ?? 60);
$saida = $p['saida'] ?? null;
$arquivoCancelar = $saida !== null ? $saida . '.cancelar' : null;

$quem = 'publicador';
$destino = caminhoDoDestino($tipoDestino, $nomeDestino);
$estado = ['estado' => 'PUBLICANDO', 'enviadas' => 0, 'total' => $quantidade, 'erro' => ''];

try {
    $stomp = conectar($endereco, $usuario, $senha);
    registrar($quem, sprintf('conectado em %s | destino=%s | entrega=%s | %d mensagens a cada %d s',
        $endereco, $destino, $persistente ? 'PERSISTENTE' : 'NAO-PERSISTENTE', $quantidade, $intervalo));
    gravarEstado($saida, $estado);

    for ($numero = 1; $numero <= $quantidade; $numero++) {
        $texto = "Mensagem $numero/$quantidade gerada em " . date('Y-m-d\TH:i:s');
        $stomp->send($destino, new Message($texto, ['persistent' => $persistente ? 'true' : 'false']));
        registrar($quem, 'ENVIADA -> ' . $texto);

        $estado['enviadas'] = $numero;
        gravarEstado($saida, $estado);

        if ($numero < $quantidade) {
            for ($s = 0; $s < $intervalo; $s++) {
                if ($arquivoCancelar !== null && file_exists($arquivoCancelar)) {
                    throw new RuntimeException('CANCELADO');
                }
                sleep(1);
                gravarEstado($saida, $estado);
            }
        }
    }

    $stomp->getClient()->disconnect();
    $estado['estado'] = 'CONCLUIDO';
    registrar($quem, "fim: $quantidade mensagens publicadas");
} catch (Throwable $e) {
    if ($e->getMessage() === 'CANCELADO') {
        $estado['estado'] = 'CANCELADO';
        registrar($quem, 'cancelado depois de ' . $estado['enviadas'] . ' mensagem(ns)');
    } else {
        $estado['estado'] = 'ERRO';
        $estado['erro'] = 'não foi possível falar com o broker (' . $endereco . '): ' . $e->getMessage();
        registrar($quem, 'ERRO: ' . $estado['erro']);
    }
}
gravarEstado($saida, $estado);
if ($arquivoCancelar !== null) {
    @unlink($arquivoCancelar);
}
exit($estado['estado'] === 'ERRO' ? 1 : 0);
