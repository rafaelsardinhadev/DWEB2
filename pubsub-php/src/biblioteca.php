<?php
declare(strict_types=1);

require_once __DIR__ . '/../vendor/autoload.php';

use Stomp\Client;
use Stomp\StatefulStomp;
use Stomp\Transport\Frame;

const ENDERECO_PADRAO = 'tcp://localhost:61613';   // porta STOMP do ActiveMQ

date_default_timezone_set('America/Sao_Paulo');

function lerArgumentos(array $argv): array
{
    $mapa = [];
    foreach (array_slice($argv, 1) as $arg) {
        if (!str_starts_with($arg, '--')) {
            continue;
        }
        $partes = explode('=', substr($arg, 2), 2);
        $mapa[$partes[0]] = $partes[1] ?? 'true';
    }
    return $mapa;
}

function registrar(string $quem, string $mensagem): void
{
    $agora = DateTime::createFromFormat('U.u', sprintf('%.6F', microtime(true)));
    $agora->setTimezone(new DateTimeZone(date_default_timezone_get()));   // 'U' sempre cria em UTC
    echo '[' . $agora->format('H:i:s.v') . '] [' . $quem . '] ' . $mensagem . PHP_EOL;
}

function caminhoDoDestino(string $tipo, string $nome): string
{
    return ($tipo === 'fila' ? '/queue/' : '/topic/') . $nome;
}

function conectar(string $endereco, string $usuario, string $senha, ?string $clientId = null): StatefulStomp
{
    $cliente = new Client($endereco);
    $cliente->setLogin($usuario, $senha);
    if ($clientId !== null) {
        $cliente->setClientId($clientId);
    }
    $cliente->connect();
    return new StatefulStomp($cliente);
}

function removerAssinaturaDuravel(StatefulStomp $stomp, string $destino, string $idDaInscricao, string $nomeDaAssinatura): void{
    $stomp->unsubscribe((int) $idDaInscricao);
    $stomp->getClient()->sendFrame(new Frame('UNSUBSCRIBE', [
        'destination' => $destino,
        'id' => $idDaInscricao,
        'activemq.subscriptionName' => $nomeDaAssinatura,
    ]));
}

function gravarEstado(?string $arquivo, array $dados): void
{
    if ($arquivo === null) {
        return;
    }
    $dados['atualizadoEm'] = time();
    $temporario = $arquivo . '.tmp' . getmypid();
    file_put_contents($temporario, json_encode($dados, JSON_UNESCAPED_UNICODE));
    @rename($temporario, $arquivo);
}
