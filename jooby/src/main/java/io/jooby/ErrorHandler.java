package io.jooby;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

public interface ErrorHandler {

  static ErrorHandler log(Logger log, StatusCode... quiet) {
    Set<StatusCode> silent = Set.of(quiet);
    return (ctx, cause, statusCode) -> {
      String message = format("%s %s %s %s", ctx.method(), ctx.pathString(),
          statusCode.value(), statusCode.reason());
      if (silent.contains(statusCode)) {
        log.info(message, cause);
      } else {
        log.error(message, cause);
      }
    };
  }

  ErrorHandler DEFAULT = (ctx, cause, statusCode) -> {
    new ContentNegotiation()
        .accept("application/json", () -> {
          String message = Optional.ofNullable(cause.getMessage()).orElse(statusCode.reason());
          return ctx.type("application/json")
              .statusCode(statusCode)
              .sendText("{\"message\":\"" + message + "\",\"statusCode\":" + statusCode.value()
                  + ",\"reason\":\"" + statusCode.reason() + "\"}");
        })
        .accept("text/html", () -> {
          String message = cause.getMessage();
          StringBuilder html = new StringBuilder("<!doctype html>\n")
              .append("<html>\n")
              .append("<head>\n")
              .append("<meta charset=\"utf-8\">\n")
              .append("<style>\n")
              .append("body {font-family: \"open sans\",sans-serif; margin-left: 20px;}\n")
              .append("h1 {font-weight: 300; line-height: 44px; margin: 25px 0 0 0;}\n")
              .append("h2 {font-size: 16px;font-weight: 300; line-height: 44px; margin: 0;}\n")
              .append("footer {font-weight: 300; line-height: 44px; margin-top: 10px;}\n")
              .append("hr {background-color: #f7f7f9;}\n")
              .append("div.trace {border:1px solid #e1e1e8; background-color: #f7f7f9;}\n")
              .append("p {padding-left: 20px;}\n")
              .append("p.tab {padding-left: 40px;}\n")
              .append("</style>\n")
              .append("<title>")
              .append(statusCode)
              .append("</title>\n")
              .append("<body>\n")
              .append("<h1>").append(statusCode.reason()).append("</h1>\n")
              .append("<hr>\n");

          if (message != null && !message.equals(statusCode.toString())) {
            html.append("<h2>message: ").append(message).append("</h2>\n");
          }
          html.append("<h2>status code: ").append(statusCode.value()).append("</h2>\n");

          html.append("</body>\n")
              .append("</html>");

          return ctx
              .type(MediaType.html)
              .statusCode(statusCode)
              .sendText(html.toString());
        }).render(ctx);
  };

  @Nonnull void apply(@Nonnull Context ctx, @Nonnull Throwable cause,
      @Nonnull StatusCode statusCode);

  @Nonnull default ErrorHandler then(@Nonnull ErrorHandler next) {
    return (ctx, cause, statusCode) -> {
      apply(ctx, cause, statusCode);
      if (!ctx.isResponseStarted()) {
        next.apply(ctx, cause, statusCode);
      }
    };
  }
}