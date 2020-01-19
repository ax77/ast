package ast.parse;

public abstract class NodeTemp {
  private static long iter = 0;

  public static long gettemp() {
    return iter++;
  }
}
