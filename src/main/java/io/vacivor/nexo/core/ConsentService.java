package io.vacivor.nexo.core;

import java.util.Set;

public interface ConsentService {

  boolean hasConsent(String subject, String clientId, Set<String> scopes);

  void approveConsent(String subject, String clientId, Set<String> scopes);
}
