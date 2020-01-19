package ast.stmt;

import ast.expr.main.CExpression;
import ast.stmt.main.CStatement;

public class Sif {
  private CExpression ifexpr;
  private CStatement ifstmt;
  private CStatement ifelse;

  public Sif(CExpression ifexpr, CStatement ifstmt) {
    this.ifexpr = ifexpr;
    this.ifstmt = ifstmt;
  }

  public Sif(CExpression ifexpr, CStatement ifstmt, CStatement ifelse) {
    this.ifexpr = ifexpr;
    this.ifstmt = ifstmt;
    this.ifelse = ifelse;
  }

  public CExpression getIfexpr() {
    return ifexpr;
  }

  public void setIfexpr(CExpression ifexpr) {
    this.ifexpr = ifexpr;
  }

  public CStatement getIfstmt() {
    return ifstmt;
  }

  public void setIfstmt(CStatement ifstmt) {
    this.ifstmt = ifstmt;
  }

  public CStatement getIfelse() {
    return ifelse;
  }

  public void setIfelse(CStatement ifelse) {
    this.ifelse = ifelse;
  }

}
