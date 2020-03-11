package ast;

import static org.junit.Assert.*;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.parse.Parse;
import ast.unit.TranslationUnit;

public class Test_FromFiles {

  private static final String MAIN_TEST_DIR = "cc_tests";

  @Test
  public void testSimpleFile() throws IOException {
    final String dir = System.getProperty("user.dir");
    final String fname = dir + "\\" + MAIN_TEST_DIR + "\\f01_main.c";

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(fname, true)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    assertEquals(1, unit.countOfFunctionDefinitions());

  }

}
