package com.exemplo.noticias.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de senhas com PBKDF2 + sal aleatorio. A senha nunca e guardada em texto puro.
 * Formato gravado no banco: iteracoes:sal:hash (sal e hash em Base64).
 */
public final class Senha {

    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SAL = 16;
    private static final int BITS_HASH = 256;

    private Senha() {
    }

    public static String gerarHash(String senha) {
        byte[] sal = new byte[TAMANHO_SAL];
        new SecureRandom().nextBytes(sal);
        byte[] hash = pbkdf2(senha, sal, ITERACOES);
        return ITERACOES + ":" + Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean confere(String senha, String armazenado) {
        try {
            String[] partes = armazenado.split(":");
            int iteracoes = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = pbkdf2(senha, sal, iteracoes);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponivel", e);
        }
    }
}
