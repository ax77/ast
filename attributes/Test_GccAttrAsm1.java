package ast.attributes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.attributes.asm.AsmList;
import ast.attributes.asm.ParseAsm;
import ast.attributes.gnuc.ParseAttributesGcc;
import ast.parse.Parse;

public class Test_GccAttrAsm1 {

  @Test
  public void testSkipAttrsAndAsm() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("__attribute__((__format__ (__scanf__, 2, 0)))\n");
    sb.append("__attribute__((__format__ (__printf__, 2, 3)))\n");
    sb.append("__attribute__((availability(macosx,introduced=10.7)))\n");
    sb.append("__attribute__((__availability__(swift, unavailable, message=\"Use vsnprintf instead.\")))\n");
    sb.append("__asm __volatile__ (\"_\" \"fdopen\" )\n");
    sb.append("1 2");

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);

    AttributeList list = new ParseAttributesGcc(p).parse();
    AsmList asmList = new ParseAsm(p).parse();

    assertEquals("1", p.tok().getValue());

    p.move();
    assertEquals("2", p.tok().getValue());
  }

}
