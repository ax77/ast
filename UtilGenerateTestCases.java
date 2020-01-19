package ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class UtilGenerateTestCases {

  private static final String TEST_OUT_TMP_TXT = "__test_out__tmp__.txt";
  private static final String TEST_SOURCES_FOLDER_NAME = "/test_sources/";
  private static final int LINE_LEN = 80;

  private String pad(String s, int c) {
    String pad = "";
    for (int i = 0; i < c - s.length(); ++i) {
      pad += " ";
    }
    return pad;
  }

  private String lineno(int n) {
    return " /*" + String.format("%03d", n) + "*/  ";
  }

  private void search(final String pattern, final File folder, List<String> result) {
    for (final File f : folder.listFiles()) {

      if (f.isDirectory()) {
        search(pattern, f, result);
      }

      if (f.isFile()) {
        if (f.getName().matches(pattern)) {
          result.add(f.getAbsolutePath());
        }
      }

    }
  }

  private List<String> fileToLines(final String path) {
    List<String> lines = new ArrayList<>();

    try {
      File f = new File(path);
      BufferedReader bufreader = new BufferedReader(new FileReader(f));

      String readLine = "";
      while ((readLine = bufreader.readLine()) != null) {

        readLine = readLine.replaceAll("\t", "    ");
        readLine = escape(readLine);
        lines.add(readLine);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return lines;
  }

  private String escape(String readLine) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < readLine.length(); i++) {
      char c = readLine.charAt(i);
      boolean isEsc = c == '\\' || c == '\"' || c == '\'';
      if (!isEsc) {
        sb.append(c);
        continue;
      }
      sb.append("\\");
      sb.append(c);
    }

    return sb.toString();
  }

  private String fileToStrBuilder(List<String> lines, int testno, String fname, List<String> config) {

    final String q = "\"";
    final String n = "\\n";
    final String testname = String.format("%03d", testno);

    StringBuilder sb = new StringBuilder();
    sb.append("StringBuilder sb_" + testname + " = new StringBuilder();\n");

    int cnt = 1;
    for (String line : lines) {
      String tmp = line.trim();
      if (tmp.isEmpty()) {
        continue;
      }
      sb.append("sb_" + testname + ".append(" + q + lineno(cnt++) + line + pad(line, LINE_LEN) + n + q + ");\n");

    }
    sb.append("\n");

    config.add("tests.add(new TestDto(false, " + fname + ", " + "sb_" + testname + "));\n");
    return sb.toString();
  }

  @Ignore
  @Test
  public void test() throws IOException {

    final String dir = System.getProperty("user.dir");
    final File folder = new File(dir + TEST_SOURCES_FOLDER_NAME);

    if (!folder.exists()) {
      throw new RuntimeException("file does not exists: " + folder.getAbsolutePath());
    }

    List<String> result = new ArrayList<>();
    List<String> config = new ArrayList<>();

    search(".*\\.c", folder, result);

    FileWriter writer = new FileWriter(TEST_OUT_TMP_TXT);

    int testno = 0;
    for (String s : result) {
      List<String> lines = fileToLines(s);
      String format_ = String.format("\"%03d\"", testno);
      writer.write(fileToStrBuilder(lines, testno, format_, config));
      testno++;
    }
    for (String s : config) {
      writer.write(s);
    }

    writer.close();
    System.out.println(":ok:");

  }
}
