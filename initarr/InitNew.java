package ast.initarr;

import ast.expr.main.CExpression;

public class InitNew {
  private int offset;
  private final CExpression expression;

  public InitNew(CExpression expression) {
    this.expression = expression;
  }

  public int getOffset() {
    return offset;
  }

  public CExpression getExpression() {
    return expression;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return offset + " = " + expression;
  }

}
