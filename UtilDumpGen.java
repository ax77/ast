package ast;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class UtilDumpGen {

  private static List<String> ENAMES = new ArrayList<String>();
  static {
    ENAMES.add("EASSIGN       ");
    ENAMES.add("EBINARY       ");
    ENAMES.add("ECOMMA        ");
    ENAMES.add("ETERNARY      ");
    ENAMES.add("EUNARY        ");
    ENAMES.add("ECOMPSEL      ");
    ENAMES.add("ECAST         ");
    ENAMES.add("EFCALL        ");
    ENAMES.add("ESUBSCRIPT    ");
    ENAMES.add("EPREINCDEC    ");
    ENAMES.add("EPOSTINCDEC   ");
    ENAMES.add("ECOMPLITERAL  ");
    ENAMES.add("EPRIMARY_IDENT");
    ENAMES.add("EPRIMARY_CONST");
    ENAMES.add("EPRIMARY_STRING");
    ENAMES.add("EPRIMARY_GENERIC");
  }

  public static List<String> SNAMES = new ArrayList<String>();
  static {
    SNAMES.add("SCOMPOUND  ");
    SNAMES.add("SIF        ");
    SNAMES.add("SWHILE     ");
    SNAMES.add("SDOWHILE   ");
    SNAMES.add("SEXPR      ");
    SNAMES.add("SBREAK     ");
    SNAMES.add("SCONTINUE  ");
    SNAMES.add("SSEMICOLON ");
    SNAMES.add("SSWITCH    ");
    SNAMES.add("SCASE      ");
    SNAMES.add("SFOR       ");
    SNAMES.add("SRETURN    ");
    SNAMES.add("SGOTO      ");
    SNAMES.add("SLABEL     ");
    SNAMES.add("SDEFAULT   ");
    SNAMES.add("SASM       ");
  }

  private static String methodName(String ename) {
    String classname = className(ename);
    return "public void dump_" + classname + "(" + classname + " e)";
  }

  private static String className(String ename) {
    String firstchar = ename.substring(0, 1);
    String tail = ename.substring(1);
    return firstchar.toUpperCase() + tail.toLowerCase();
  }

  private static String otagname(String ename, String oc) {
    return "    " + oc + "tag(tags." + ename + ");\n";
  }

  private static String gcomm(String how) {
    return "//////////////////////////////////////////////////////////////////////\n" + "// " + how.toUpperCase()
        + "\n";
  }

  private static void gifs(List<String> listof, String basename, String classname) {
    // private void dumpe(CExpression e) {
    //    if(base == CExpressionBase.EUNARY) {
    //      dump_Eunary(e.getEunary());
    //    }
    // }
    StringBuilder sb = new StringBuilder();
    sb.append("private void dump_" + classname + "(" + classname + " e) {\n");

    for (String s : listof) {
      String ename = s.trim();
      sb.append("  if(base == " + basename + "." + ename + ") {\n");
      sb.append("    dump_" + className(ename) + "(e.get" + className(ename) + "());\n");
      sb.append("  }\n\n");
    }
    sb.append("}\n\n");

    System.out.println(sb.toString());
  }

  private static void g(List<String> listof) {
    for (String s : listof) {
      String ename = s.trim();
      StringBuilder sb = new StringBuilder();
      sb.append(methodName(ename));
      sb.append("\n{\n");
      sb.append(otagname(ename, "o"));
      sb.append("\n    // TODO: implement\n\n");
      sb.append(otagname(ename, "c"));
      sb.append("}\n");
      System.out.println(sb.toString());
    }
  }

  @Ignore
  @Test
  public void test() {
    System.out.println(gcomm("expressions"));

    for (String s : ENAMES) {
      System.out.println("case " + s + ": {");
      System.out.println("break;");
      System.out.println("}");
    }
    System.out.println("default:{}");

    //    g(ENAMES);
    //    gifs(ENAMES, "CExpressionBase", "CExpression");
    //
    //    System.out.println(gcomm("statements"));
    //    g(SNAMES);
    //    gifs(SNAMES, "CStatementBase", "CStatement");
  }
}
