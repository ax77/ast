package ast.declarations;

import java.util.ArrayList;
import java.util.List;

import ast.expr.main.CExpression;
import ast.parse.ParseException;

public class Initializer {

  //  initializer
  //    : assignment_expression
  //    | '{' initializer_list '}'
  //    | '{' initializer_list ',' '}'
  //    ;

  private final List<Integer> index;
  private final boolean hasInitializerList;
  private final CExpression assignment;
  private final InitializerList initializerList;

  public Initializer(CExpression assignment) {
    if (assignment == null) {
      //TODO:POSITION
      throw new ParseException("assignment==null in new initializer...");
    }

    this.index = new ArrayList<Integer>(0);
    this.hasInitializerList = false;
    this.assignment = assignment;
    this.initializerList = null;
  }

  public Initializer(InitializerList initializerList) {
    if (initializerList == null) {
      //TODO:POSITION
      throw new ParseException("initializer-list==null in new initializer...");
    }

    this.index = new ArrayList<Integer>(0);
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

  public InitializerList getInitializerList() {
    return initializerList;
  }

  public void pushi(int i) {
    index.add(i);
  }

  public void pushall(List<Integer> outer) {
    index.addAll(0, outer);
  }

  public List<Integer> getIndex() {
    return index;
  }

}
