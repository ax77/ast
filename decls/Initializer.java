package ast.decls;

import ast.expr.CExpression;

public class Initializer implements Comparable<Initializer> {
  private final CExpression init;
  private final int offset;

  public Initializer(CExpression init, int offset) {
    this.init = init;
    this.offset = offset;
  }

  public CExpression getInit() {
    return init;
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return String.format("%d=%s", offset, init);
  }

  @Override
  public int compareTo(Initializer o) {
    if (offset < o.getOffset()) {
      return -1;
    }
    if (offset > o.getOffset()) {
      return 1;
    }
    return 0;
  }
}