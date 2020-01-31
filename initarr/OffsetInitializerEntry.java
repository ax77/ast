package ast.initarr;

import ast.expr.main.CExpression;

public class OffsetInitializerEntry {

  private int level;
  private CExpression expression;

  public OffsetInitializerEntry(int level, CExpression expression) {
    this.level = level;
    this.expression = expression;
  }

  public int getLevel() {
    return level;
  }

  public CExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    return expression.toString();// String.format("%-3d", weight) + " = " + expression;
  }

}