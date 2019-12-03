package io.jooby.pac4j;

import io.jooby.Context;
import io.jooby.internal.pac4j.WebContextImpl;
import org.pac4j.core.context.WebContext;

public interface Pac4jContext extends WebContext {
  Context getContext();

  static Pac4jContext create(Context ctx) {
    return new WebContextImpl(ctx);
  }
}
