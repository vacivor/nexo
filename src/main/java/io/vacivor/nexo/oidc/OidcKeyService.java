package io.vacivor.nexo.oidc;

import jakarta.inject.Singleton;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OidcKeyService {

  private static final Logger LOG = LoggerFactory.getLogger(OidcKeyService.class);
  private static final Base64.Decoder DECODER = Base64.getDecoder();

  private final String algorithm;
  private final String keyId;
  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  public OidcKeyService(OidcConfiguration configuration) {
    this.algorithm = configuration.getSigningAlgorithm();
    this.keyId = configuration.getKeyId();

    if ("RS256".equalsIgnoreCase(algorithm)) {
      KeyPair keyPair = loadOrGenerateRsa(configuration);
      this.privateKey = keyPair.getPrivate();
      this.publicKey = keyPair.getPublic();
    } else {
      this.privateKey = null;
      this.publicKey = null;
    }
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public String getKeyId() {
    return keyId;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public boolean isRsa() {
    return publicKey instanceof RSAPublicKey && privateKey != null;
  }

  private KeyPair loadOrGenerateRsa(OidcConfiguration configuration) {
    String privateKeyBase64 = configuration.getRsaPrivateKey();
    String publicKeyBase64 = configuration.getRsaPublicKey();
    if (privateKeyBase64 != null && publicKeyBase64 != null) {
      try {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] privateBytes = DECODER.decode(privateKeyBase64);
        byte[] publicBytes = DECODER.decode(publicKeyBase64);
        PrivateKey priv = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        PublicKey pub = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
        return new KeyPair(pub, priv);
      } catch (Exception e) {
        throw new IllegalStateException("Failed to load RSA keys", e);
      }
    }

    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      LOG.warn("OIDC RSA keys not configured; generating ephemeral key pair");
      return generator.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate RSA key pair", e);
    }
  }
}
