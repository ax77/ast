package ast.declarations;

import java.util.List;

import jscan.sourceloc.SourceLocation;
import ast.parse.ILocation;
import ast.parse.NullChecker;

public class InitializerListEntry implements ILocation {

  private final SourceLocation location;
  private final boolean isHasDesignatorsBefore;
  private final List<Designator> designators;
  private final Initializer initializer;

  public InitializerListEntry(List<Designator> designators, Initializer initializer, SourceLocation location) {
    NullChecker.check(designators, initializer);

    this.location = location;
    this.isHasDesignatorsBefore = true;
    this.designators = designators;
    this.initializer = initializer;
  }

  public InitializerListEntry(Initializer initializer, SourceLocation location) {
    NullChecker.check(initializer);

    this.location = location;
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

  @Override
  public String toString() {
    if (isHasDesignatorsBefore) {
      return designators + "=" + initializer;
    }
    return initializer.toString();
  }

  @Override
  public SourceLocation getLocation() {
    return location;
  }

  @Override
  public String getLocationToString() {
    return location.toString();
  }

  @Override
  public int getLocationLine() {
    return location.getLine();
  }

  @Override
  public int getLocationColumn() {
    return location.getColumn();
  }

  @Override
  public String getLocationFile() {
    return location.getFilename();
  }

}
