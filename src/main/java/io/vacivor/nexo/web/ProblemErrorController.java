package io.vacivor.nexo.web;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.vacivor.nexo.exception.ProblemDetails;

@Controller
public class ProblemErrorController {

  @Error(global = true, status = HttpStatus.NOT_FOUND)
  public HttpResponse<ProblemDetails> notFound(HttpRequest<?> request) {
    ProblemDetails problem = new ProblemDetails(
        "about:blank",
        HttpStatus.NOT_FOUND.getReason(),
        HttpStatus.NOT_FOUND.getCode(),
        "Resource not found",
        request.getPath()
    );
    return HttpResponse.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
        .body(problem);
  }

  @Error(global = true, status = HttpStatus.BAD_REQUEST)
  public HttpResponse<ProblemDetails> badRequest(HttpRequest<?> request) {
    ProblemDetails problem = new ProblemDetails(
        "about:blank",
        HttpStatus.BAD_REQUEST.getReason(),
        HttpStatus.BAD_REQUEST.getCode(),
        "Request parameters are invalid",
        request.getPath()
    );
    return HttpResponse.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
        .body(problem);
  }

  @Error(global = true, status = HttpStatus.METHOD_NOT_ALLOWED)
  public HttpResponse<ProblemDetails> methodNotAllowed(HttpRequest<?> request) {
    ProblemDetails problem = new ProblemDetails(
        "about:blank",
        HttpStatus.METHOD_NOT_ALLOWED.getReason(),
        HttpStatus.METHOD_NOT_ALLOWED.getCode(),
        "Method not allowed",
        request.getPath()
    );
    return HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
        .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
        .body(problem);
  }

  @Error(global = true, status = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public HttpResponse<ProblemDetails> unsupportedMediaType(HttpRequest<?> request) {
    ProblemDetails problem = new ProblemDetails(
        "about:blank",
        HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReason(),
        HttpStatus.UNSUPPORTED_MEDIA_TYPE.getCode(),
        "Unsupported media type",
        request.getPath()
    );
    return HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
        .body(problem);
  }
}
