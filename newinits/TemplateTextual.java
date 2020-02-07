package ast.newinits;

import java.util.List;

public class TemplateTextual {

  public static String gTemplate(List<Integer> arrinfo) {
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
    if (prev.startsWith("{") && prev.endsWith("}")) {
      prev = prev.substring(1, prev.length() - 1);
    } else {
      throw new RuntimeException("unexpected template: " + prev);
    }
    return prev;
  }

  private static String gLast(int howMuch) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (int i = 0; i < howMuch; i++) {
      sb.append("0 ");
    }
    sb.append("}");
    return sb.toString();
  }

  private static String gTop(int howMuch, String prev) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (int i = 0; i < howMuch; i++) {
      sb.append(prev);
    }
    sb.append("}");
    return sb.toString();
  }

}
