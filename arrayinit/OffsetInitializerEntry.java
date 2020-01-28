package ast.arrayinit;

import java.util.List;

import ast.expr.main.CExpression;

public class OffsetInitializerEntry {

  private final List<Integer> index;
  private final CExpression expression;

  public OffsetInitializerEntry(List<Integer> index, CExpression expression) {
    this.index = index;
    this.expression = expression;
  }

  public List<Integer> getIndex() {
    return index;
  }

  public CExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    return index.toString() + ":" + expression.toString();
  }

}