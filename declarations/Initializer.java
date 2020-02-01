package ast.declarations;

import java.util.List;

import ast.errors.ParseException;
import ast.expr.main.CExpression;

public class Initializer {

  //  initializer
  //    : assignment_expression
  //    | '{' initializer_list '}'
  //    | '{' initializer_list ',' '}'
  //    ;

  private final boolean hasInitializerList;
  private final CExpression assignment;
  private final List<InitializerListEntry> initializerList;

  public Initializer(CExpression assignment) {
    if (assignment == null) {
      //TODO:POSITION
      throw new ParseException("assignment==null in new initializer...");
    }

    this.hasInitializerList = false;
    this.assignment = assignment;
    this.initializerList = null;
  }

  public Initializer(List<InitializerListEntry> initializerList) {
    if (initializerList == null) {
      //TODO:POSITION
      throw new ParseException("initializer-list==null in new initializer...");
    }

    this.hasInitializerList = true;
    this.assignment = null;
    this.initializerList = initializerList;
  }

  public boolean isHasInitializerList() {
    return hasInitializerList;
  }

  public CExpression getAssignment() {
    return assignment;
  }

  public List<InitializerListEntry> getInitializerList() {
    return initializerList;
  }

}
