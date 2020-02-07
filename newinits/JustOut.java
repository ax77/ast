package ast.newinits;

import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.expr.main.CExpression;

public class JustOut {
  //temp-fields. not used after semantic analyzer
  private int level;
  private final Token op;

  private int offset;
  private final CExpression ex;

  public JustOut(Token op, CExpression ex) {
    this.op = op;
    this.ex = ex;
  }

  public JustOut(CExpression ex) {
    this.op = null;
    this.ex = ex;
  }

  public Token getOp() {
    return op;
  }

  public CExpression getEx() {
    return ex;
  }

  @Override
  public String toString() {
    return (op == null ? "" : op.getValue()) + (ex == null ? "" : ex);
  }

  public boolean isOpen() {
    return op != null && op.ofType(T.T_LEFT_BRACE);
  }

  public boolean isClose() {
    return op != null && op.ofType(T.T_RIGHT_BRACE);
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

}