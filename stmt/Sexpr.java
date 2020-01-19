package ast.stmt;

import ast.expr.main.CExpression;

public class Sexpr {
  private CExpression expression;

  public Sexpr(CExpression expression) {
    super();
    this.expression = expression;
  }

  public CExpression getExpression() {
    return expression;
  }

  public void setExpression(CExpression expression) {
    this.expression = expression;
  }

}
