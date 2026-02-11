package io.vacivor.nexo;

public interface ClientDetailsService {

  ClientDetails loadClientByClientId(String clientId);

}
