package ast;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import jscan.Tokenlist;
import jscan.tokenize.Token;

import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast.attributes.Attribute;
import ast.attributes.main.AttributesAsmsLists;
import ast.attributes.main.ParseAttributesAsms;
import ast.parse.Parse;

public class Test_AttrC2x {

  @Test
  public void testSimpleAttributes_C2X() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("[[fallthrough]] [[__fallthrough__]] [[ deprecated, hal:: daisy]]\n");
    sb.append("1 2");

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);

    AttributesAsmsLists attributesAsmsLists = new ParseAttributesAsms(p).parse();

    assertEquals("1", p.tok().getValue());

    p.move();
    assertEquals("2", p.tok().getValue());

    boolean print = false;
    if (print) {
      for (Attribute attr : attributesAsmsLists.getAttributeListC2x().getAttributes()) {
        for (Token tok : attr.getTokens()) {
          System.out.printf("%s ", tok.getValue());
        }
        System.out.println();
      }
    }
  }

}
