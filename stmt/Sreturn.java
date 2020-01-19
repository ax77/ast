package ast.stmt;

import ast.expr.main.CExpression;

public class Sreturn {
  private final CExpression retexpr; // must be null... OPT

  public Sreturn() {
    this.retexpr = null;
  }

  public Sreturn(CExpression retexpr) {
    this.retexpr = retexpr;
  }

  public CExpression getRetexpr() {
    return retexpr;
  }

}
