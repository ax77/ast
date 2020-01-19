package ast.declarations;

import java.util.ArrayList;
import java.util.List;

import ast.parse.ParseException;

public class Designation {
  private final List<Designator> designators;

  public Designation() {
    this.designators = new ArrayList<Designator>(0);
  }

  public void push(Designator designator) {
    //TODO:POSITION
    if (designator == null) {
      throw new ParseException("null designator... TODO...");
    }
    designators.add(designator);
  }

  public List<Designator> getDesignators() {
    return designators;
  }
}
