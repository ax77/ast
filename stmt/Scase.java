package ast.stmt;

import ast.expr.main.CExpression;
import ast.stmt.main.CStatement;
import ast.stmt.main.TempLabel;

public class Scase {

  private final String labelout;
  private final Sswitch parent;
  private CExpression constexpr;
  private CStatement casestmt;

  public Scase(Sswitch parent, CExpression constexpr) {
    this.labelout = TempLabel.getendcase();
    this.parent = parent;
    this.constexpr = constexpr;
  }

  public CExpression getConstexpr() {
    return constexpr;
  }

  public void setConstexpr(CExpression constexpr) {
    this.constexpr = constexpr;
  }

  public CStatement getCasestmt() {
    return casestmt;
  }

  public void setCasestmt(CStatement casestmt) {
    this.casestmt = casestmt;
  }

  public Sswitch getParent() {
    return parent;
  }

  public String getLabelout() {
    return labelout;
  }

}
