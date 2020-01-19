package ast.stmt;

import ast.expr.main.CExpression;
import ast.stmt.main.CStatement;

public class Swhile {
  private CExpression cond;
  private CStatement loop;

  public Swhile(CExpression cond, CStatement loop) {
    this.cond = cond;
    this.loop = loop;
  }

  public CExpression getCond() {
    return cond;
  }

  public void setCond(CExpression cond) {
    this.cond = cond;
  }

  public CStatement getLoop() {
    return loop;
  }

  public void setLoop(CStatement loop) {
    this.loop = loop;
  }

}
