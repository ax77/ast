package ast.join;

import ast.expr.main.CExpression;

public class InitNew implements Comparable<InitNew> {
  private final CExpression init;
  private int offset;

  public InitNew(CExpression init) {
    this.init = init;
    this.offset = -1;
  }

  public CExpression getInit() {
    return init;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return String.format("%d=%s", offset, init);
  }

  @Override
  public int compareTo(InitNew o) {
    if (offset < o.getOffset()) {
      return -1;
    }
    if (offset > o.getOffset()) {
      return 1;
    }
    return 0;
  }
}