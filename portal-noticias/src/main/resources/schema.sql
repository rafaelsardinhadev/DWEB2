CREATE TABLE IF NOT EXISTS empresa (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL UNIQUE,
    conflitos  VARCHAR(255) DEFAULT '' NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    login       VARCHAR(30)  NOT NULL UNIQUE,
    senha_hash  VARCHAR(200) NOT NULL,
    tipo        VARCHAR(10)  NOT NULL CHECK (tipo IN ('ADMIN', 'COMUM'))
);

-- Um usuario pode pertencer a mais de uma empresa (ex.: um administrador).
CREATE TABLE IF NOT EXISTS usuario_empresa (
    usuario_id  INT NOT NULL,
    empresa_id  INT NOT NULL,
    PRIMARY KEY (usuario_id, empresa_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresa (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS noticia (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id       INT NOT NULL,
    autor            VARCHAR(100) NOT NULL,
    titulo           VARCHAR(150) NOT NULL,
    texto            VARCHAR(5000) NOT NULL,
    data_publicacao  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (empresa_id) REFERENCES empresa (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS conta (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  INT NOT NULL,
    descricao   VARCHAR(120) NOT NULL,
    valor       DECIMAL(12, 2) NOT NULL,
    vencimento  DATE NOT NULL,
    tipo        VARCHAR(10) NOT NULL CHECK (tipo IN ('PAGAR', 'RECEBER')),
    paga        BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresa (id) ON DELETE CASCADE
);
