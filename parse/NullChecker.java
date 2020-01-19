package ast.parse;

public abstract class NullChecker {

  public static void check(Object... what) {
    for (Object o : what) {
      if (o == null) {
        throw new ParseException("non-nullable property was null...");
      }
    }
  }

}
