package io.vacivor.nexo.security.auth;

public interface ClientDetailsService {

  ClientDetails loadClientByClientId(String clientId);

}
