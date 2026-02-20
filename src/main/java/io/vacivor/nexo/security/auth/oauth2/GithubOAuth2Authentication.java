package io.vacivor.nexo.security.auth.oauth2;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

public class GithubOAuth2Authentication extends OAuth2Authentication {

  public static final String PROVIDER = "github";

  public GithubOAuth2Authentication(String code, String redirectUri) {
    super(PROVIDER, code, redirectUri);
  }
}