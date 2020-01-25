package ast.declarations;

import ast.errors.ParseException;

public class InitializerListEntry {

  private final boolean isDesignation;
  private final DesignatedInitializer designatedInitializer;
  private final Initializer initializer;

  public InitializerListEntry(DesignatedInitializer designatedInitializer) {
    if (designatedInitializer == null) {
      //TODO:POSITION
      throw new ParseException("null designatedInitializer...");
    }

    this.isDesignation = true;
    this.designatedInitializer = designatedInitializer;
    this.initializer = null;
  }

  public InitializerListEntry(Initializer initializer) {
    if (initializer == null) {
      //TODO:POSITION
      throw new ParseException("null initializer...");
    }

    this.isDesignation = false;
    this.designatedInitializer = null;
    this.initializer = initializer;
  }

  public DesignatedInitializer getDesignatedInitializer() {
    return designatedInitializer;
  }

  public Initializer getInitializer() {
    return initializer;
  }

  public boolean isDesignation() {
    return isDesignation;
  }

}
