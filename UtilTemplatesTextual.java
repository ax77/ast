package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

public class UtilTemplatesTextual {

  //  template = {{0,0,0},{0,0,0}}
  //
  //  List<JustOut> template = new ArrayList<JustOut>();
  //  template.add(joLbr());
  //  //
  //  template.add(joLbr());
  //  template.add(joZero());
  //  template.add(joZero());
  //  template.add(joZero());
  //  template.add(joRbr());
  //  //
  //  template.add(joLbr());
  //  template.add(joZero());
  //  template.add(joZero());
  //  template.add(joZero());
  //  template.add(joRbr());
  //  //
  //  template.add(joRbr());

  private static int getRandomNumberInRange(int min, int max) {

    Random r = new Random();

    if (min >= max) {
      throw new IllegalArgumentException("max must be greater than min");
    }

    return r.nextInt((max - min) + 1) + min;
  }

  private static String gLast(int howMuch) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (int i = 0; i < howMuch; i++) {
      int n = getRandomNumberInRange(0, getRandomNumberInRange(i, 120));
      sb.append(String.format("%d,", n)); // "0,"
    }
    sb.append("},");
    return sb.toString();
  }

  private static String gTop(int howMuch, String prev) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (int i = 0; i < howMuch; i++) {
      sb.append(prev);
    }
    sb.append("},");
    return sb.toString();
  }

  private static String gTemplate(List<Integer> arrinfo) {
    if (arrinfo == null || arrinfo.isEmpty()) {
      throw new RuntimeException("unexpected empty input...");
    }
    String prev = null;
    boolean islast = true;
    while (!arrinfo.isEmpty()) {
      int h = arrinfo.remove(arrinfo.size() - 1);
      if (islast) {
        prev = gLast(h);
        islast = false;
      } else {
        if (prev == null) {
          throw new RuntimeException("unexpected null...");
        }
        String next = gTop(h, prev);
        prev = next;
      }
    }
    return prev;
  }

  /////temps

  private static int cnt = 0;
  private static List<String> vars = new ArrayList<String>();

  private static List<Integer> genArrayDims() {
    List<Integer> r = new ArrayList<Integer>();
    for (int i = 0; i < getRandomNumberInRange(1, 5); i++) {
      r.add(getRandomNumberInRange(1, 4));
    }
    return r;
  }

  private static void garr() {

    List<Integer> arrinfo = genArrayDims();
    StringBuilder sb = new StringBuilder();

    final String varname = String.format("arr_%02d", cnt++);
    vars.add(varname);

    sb.append("    int " + varname);

    int x = 0;
    for (Integer i : arrinfo) {
      if (x == 0) {
        sb.append("[]");
      } else {
        sb.append(String.format("[%d]", i));
      }
      ++x;
    }

    String template = gTemplate(arrinfo);
    if (template.endsWith(",")) {
      template = template.substring(0, template.length() - 1);
    }

    sb.append(" = ");
    sb.append(template);
    sb.append(" ;");

    System.out.println(sb.toString());
  }

  @Ignore
  @Test
  public void test() {

    cnt = 0;
    for (int i = 0; i < 32; i++) {
      garr();
    }

    // printf("%s == %d\n", "arr_00", sizeof(arr_00)/sizeof(arr_00[0]));

    String q2 = "\"";
    for (String s : vars) {
      System.out.println("    printf(\"%s == %d\\n\", "
          + q2
          + s
          + q2
          + ","
          + "sizeof("
          + s
          + ") / "
          + "sizeof("
          + s
          + "[0]));");
    }

    //    int x[] = { 1, 2, 3 };
    //    String template = "{ @ @ }";
    //    String tmps[] = { "", "{ @ }", "0 0 0" };
    //
    //    for (int i = 0; i < x.length - 1; i++) {
    //      template = template.replaceAll("@", tmps[i + 1]);
    //      System.out.println();
    //    }
    //
    //    System.out.println(template);

  }

}
