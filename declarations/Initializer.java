package ast.declarations;

import java.util.List;

import jscan.sourceloc.SourceLocation;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.parse.ILocation;

public class Initializer implements ILocation {

  //  initializer
  //    : assignment_expression
  //    | '{' initializer_list '}'
  //    | '{' initializer_list ',' '}'
  //    ;

  private final SourceLocation location;
  private final boolean isInitializerList;
  private final CExpression assignment;
  private final List<InitializerListEntry> initializerList;

  public Initializer(CExpression assignment, SourceLocation location) {
    if (assignment == null) {
      //TODO:POSITION
      throw new ParseException("assignment==null in new initializer...");
    }

    this.location = location;
    this.isInitializerList = false;
    this.assignment = assignment;
    this.initializerList = null;
  }

  public Initializer(List<InitializerListEntry> initializerList, SourceLocation location) {
    if (initializerList == null) {
      //TODO:POSITION
      throw new ParseException("initializer-list==null in new initializer...");
    }

    this.location = location;
    this.isInitializerList = true;
    this.assignment = null;
    this.initializerList = initializerList;
  }

  public boolean isInitializerList() {
    return isInitializerList;
  }

  public CExpression getAssignment() {
    return assignment;
  }

  public List<InitializerListEntry> getInitializerList() {
    return initializerList;
  }

  @Override
  public String toString() {
    if (isInitializerList) {
      return "" + initializerList;
    }
    return assignment.toString();
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
