<?php

declare(strict_types=1);

require_once __DIR__ . '/../src/biblioteca.php';

header('Content-Type: application/json; charset=UTF-8');
header('Cache-Control: no-store');

const RAIZ = __DIR__ . '/..';
const PASTA = RAIZ . '/dados';
const PUBLICADOR = PASTA . '/publicador.json';
const NOME_DO_DESTINO = 'lab.php.noticias';

@mkdir(PASTA, 0777, true);

function responder(array $dados, int $status = 200): never
{
    http_response_code($status);
    echo json_encode($dados, JSON_UNESCAPED_UNICODE);
    exit;
}

function falhar(string $mensagem, int $status = 400): never
{
    responder(['erro' => $mensagem], $status);
}

function tipoDeDestino(): string
{
    $valor = $_POST['destino'] ?? '';
    return in_array($valor, ['topico', 'fila'], true) ? $valor : falhar("Destino inválido: use 'fila' ou 'topico'.");
}

function inteiro(string $campo, string $rotulo, int $minimo, int $maximo): int
{
    $valor = filter_var($_POST[$campo] ?? '', FILTER_VALIDATE_INT);
    return ($valor !== false && $valor >= $minimo && $valor <= $maximo)
        ? $valor : falhar("$rotulo deve ser um número de $minimo a $maximo.");
}

function executar(string $script, array $argumentos, bool $esperar = false): void
{
    $comando = escapeshellarg(PHP_BINARY) . ' ' . escapeshellarg(RAIZ . "/$script");
    foreach ($argumentos as $chave => $valor) {
        $comando .= ' ' . escapeshellarg("--$chave=$valor");
    }
    if ($esperar) {
        exec($comando);
    } elseif (PHP_OS_FAMILY === 'Windows') {
        pclose(popen("start /B \"\" $comando > NUL 2>&1", 'r'));
    } else {
        exec("$comando > /dev/null 2>&1 &");
    }
}

function lerEstado(string $arquivo): ?array
{
    $dados = is_file($arquivo) ? json_decode((string) @file_get_contents($arquivo), true) : null;
    if (!is_array($dados)) {
        return null;
    }
    $semSinal = time() - ($dados['atualizadoEm'] ?? 0) > 5;
    if ($semSinal && in_array($dados['estado'], ['CONECTANDO', 'CONECTADO', 'PUBLICANDO'], true)) {
        $dados['estado'] = 'ERRO';
        $dados['erro'] = 'o processo parou sem avisar.';
    }
    return $dados;
}

function arquivosDosAssinantes(): array
{
    return glob(PASTA . '/assinante-*.json') ?: [];
}

function iniciarAssinante(int $id, array $config): void
{
    $arquivo = PASTA . "/assinante-$id.json";
    @unlink("$arquivo.parar");
    $recebidas = lerEstado($arquivo)['recebidas'] ?? [];   // religado: continua contando o que já leu
    gravarEstado($arquivo, [
        'id' => $id, 'estado' => 'CONECTANDO', 'erro' => '', 'destino' => $config['destino'],
        'nome' => $config['nome'], 'duravel' => $config['duravel'], 'alvo' => $config['alvo'], 'recebidas' => $recebidas,
    ]);
    executar('assinante.php', [
        'id' => $id, 'destino' => $config['destino'], 'nome' => $config['nome'],
        'duravel' => $config['duravel'] ? 'true' : 'false', 'quantidade' => $config['alvo'],
        'espera' => 900,
        'saida' => $arquivo,
    ]);
}

function pararAssinantes(): void
{
    $arquivos = arquivosDosAssinantes();
    foreach ($arquivos as $arquivo) {
        touch("$arquivo.parar");
    }
    if ($arquivos) {
        sleep(2);
    }
    foreach ($arquivos as $arquivo) {
        $a = lerEstado($arquivo);
        if ($a !== null && $a['duravel']) {
            executar('assinante.php', ['id' => $a['id'], 'destino' => $a['destino'], 'nome' => $a['nome'],
                'apagar-assinatura' => 'true'], true);
        }
    }
    foreach (glob(PASTA . '/assinante-*') ?: [] as $arquivo) {
        unlink($arquivo);
    }
}

// ---------- Ações ----------

$acao = $_GET['acao'] ?? '';
if ($acao !== 'estado' && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    falhar('Use POST.', 405);
}

switch ($acao) {
    case 'estado':
        $assinantes = array_values(array_filter(array_map('lerEstado', arquivosDosAssinantes())));
        usort($assinantes, fn ($a, $b) => $a['id'] <=> $b['id']);
        // Sem o horário, a resposta só muda quando algo muda de verdade e a página não redesenha os botões à toa
        $assinantes = array_map(fn ($a) => array_diff_key($a, ['atualizadoEm' => 1]), $assinantes);
        $publicador = lerEstado(PUBLICADOR) ?? ['estado' => 'OCIOSO', 'enviadas' => 0, 'total' => 0, 'erro' => ''];
        responder(['publicador' => $publicador, 'assinantes' => $assinantes]);

    case 'iniciar-assinantes':
        $config = ['destino' => tipoDeDestino(), 'nome' => NOME_DO_DESTINO, 'duravel' => ($_POST['duravel'] ?? '') === 'true'];
        if ($config['duravel'] && $config['destino'] === 'fila') {
            falhar('Assinatura durável só existe para tópico.');
        }
        $instancias = inteiro('instancias', 'O número de assinantes', 1, 10);
        $config['alvo'] = inteiro('alvo', 'A quantidade de mensagens por assinante', 1, 20);

        pararAssinantes();
        for ($id = 1; $id <= $instancias; $id++) {
            iniciarAssinante($id, $config);
        }
        responder(['ok' => true]);

    case 'parar-assinantes':
        pararAssinantes();
        responder(['ok' => true]);

    case 'conectar':
    case 'desconectar':
        $id = inteiro('id', 'O id', 1, 10);
        $arquivo = PASTA . "/assinante-$id.json";
        $assinante = lerEstado($arquivo) ?? falhar('Assinante não encontrado. Clique em "Iniciar assinantes".', 404);
        if ($acao === 'conectar') {
            iniciarAssinante($id, $assinante);
        } else {
            touch("$arquivo.parar");
        }
        responder(['ok' => true]);

    case 'publicar':
        $tipo = tipoDeDestino();
        $quantidade = inteiro('quantidade', 'A quantidade de mensagens', 1, 20);
        $intervalo = inteiro('intervalo', 'O intervalo', 0, 600);
        if ((lerEstado(PUBLICADOR)['estado'] ?? '') === 'PUBLICANDO') {
            falhar('Já existe um envio em andamento. Espere terminar ou cancele.', 409);
        }
        @unlink(PUBLICADOR . '.cancelar');
        gravarEstado(PUBLICADOR, ['estado' => 'PUBLICANDO', 'enviadas' => 0, 'total' => $quantidade, 'erro' => '']);
        executar('publicador.php', [
            'destino' => $tipo, 'nome' => NOME_DO_DESTINO, 'persistente' => ($_POST['persistente'] ?? '') === 'true' ? 'true' : 'false',
            'quantidade' => $quantidade, 'intervalo' => $intervalo, 'saida' => PUBLICADOR,
        ]);
        responder(['ok' => true]);

    case 'cancelar':
        if ((lerEstado(PUBLICADOR)['estado'] ?? '') === 'PUBLICANDO') {
            touch(PUBLICADOR . '.cancelar');
        }
        responder(['ok' => true]);

    default:
        falhar('Ação desconhecida.', 404);
}
