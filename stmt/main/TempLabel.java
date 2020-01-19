package ast.stmt.main;

public abstract class TempLabel {

  private static int doiter = 0;
  private static int enddoiter = 0;

  private static int ifiter = 0;
  private static int endifiter = 0;

  private static int switchout = 0;
  private static int caseout = 0;

  private static int foriter = 0;
  private static int endforiter = 0;

  private static int defaultiter = 0;

  public static String getfor() {
    return String.format("for_%d", foriter++);
  }

  public static String getendfor() {
    return String.format("endfor_%d", endforiter++);
  }

  public static String getdo() {
    return String.format("do_%d", doiter++);
  }

  public static String getenddo() {
    return String.format("enddo_%d", enddoiter++);
  }

  public static String getif() {
    return String.format("if_%d", ifiter++);
  }

  public static String getendif() {
    return String.format("endif_%d", endifiter++);
  }

  public static String getswout() {
    return String.format("endswitch_%d", switchout++);
  }

  public static String getendcase() {
    return String.format("endcase_%d", caseout++);
  }

  public static String getdefault() {
    return String.format("default_%d", defaultiter++);
  }

}
