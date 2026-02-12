package io.vacivor.nexo.web;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.vacivor.nexo.exception.AuthenticationException;
import io.vacivor.nexo.exception.ProblemDetails;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<ProblemDetails>> {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @Override
  public HttpResponse<ProblemDetails> handle(HttpRequest request, Throwable exception) {
    HttpStatus status = resolveStatus(exception);
    if (status.getCode() >= 500) {
      LOG.error("Unhandled exception for {} {}", request.getMethod(), request.getPath(), exception);
    } else {
      LOG.warn("Request error for {} {}: {}", request.getMethod(), request.getPath(), exception.getMessage(), exception);
    }

    String detail = Optional.ofNullable(exception.getMessage()).orElse(status.getReason());
    ProblemDetails problem = new ProblemDetails(
        "about:blank",
        status.getReason(),
        status.getCode(),
        detail,
        request.getPath()
    );

    return HttpResponse.status(status)
        .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
        .body(problem);
  }

  private HttpStatus resolveStatus(Throwable exception) {
    if (exception instanceof HttpStatusException httpStatusException) {
      return httpStatusException.getStatus();
    }
    if (exception instanceof AuthenticationException) {
      return HttpStatus.UNAUTHORIZED;
    }
    if (exception instanceof IllegalArgumentException) {
      return HttpStatus.BAD_REQUEST;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
