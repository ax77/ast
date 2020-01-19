package ast.stmt;

import ast.expr.main.CExpression;
import ast.stmt.main.CStatement;

public class Sdowhile {
  private CExpression cond;
  private CStatement loop;

  public Sdowhile(CExpression cond, CStatement loop) {
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
