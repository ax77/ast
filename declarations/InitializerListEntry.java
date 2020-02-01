package ast.declarations;

import java.util.List;

import ast.parse.NullChecker;

public class InitializerListEntry {

  private final boolean isHasDesignatorsBefore;
  private final List<Designator> designators;
  private final Initializer initializer;

  public InitializerListEntry(List<Designator> designators, Initializer initializer) {
    NullChecker.check(designators, initializer);

    this.isHasDesignatorsBefore = true;
    this.designators = designators;
    this.initializer = initializer;
  }

  public InitializerListEntry(Initializer initializer) {
    NullChecker.check(initializer);

    this.isHasDesignatorsBefore = false;
    this.designators = null;
    this.initializer = initializer;
  }

  public List<Designator> getDesignators() {
    return designators;
  }

  public Initializer getInitializer() {
    return initializer;
  }

  public boolean isHasDesignatorsBefore() {
    return isHasDesignatorsBefore;
  }

}
