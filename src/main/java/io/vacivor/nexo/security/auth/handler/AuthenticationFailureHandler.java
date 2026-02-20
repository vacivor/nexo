package io.vacivor.nexo.security.auth.handler;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.http.MutableHttpResponse;

public interface AuthenticationFailureHandler {

  void onFailure(MutableHttpResponse<?> response);
}