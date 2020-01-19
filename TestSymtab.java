package ast;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ast.symtabg.Symtab;

public class TestSymtab {

  @Test
  public void test() {
    Symtab<String, String> table = new Symtab<String, String>();
    assertTrue(table.isEmpty());

    String funcname = "main";

    table.pushscope("file");
    assertTrue(table.isFileScope());

    table.addsym("main", "function");
    String sym = table.getsym("main");
    assertNotNull("sym test main", sym);

    table.pushscope(funcname);
    sym = table.getsymFromCurrentScope("main");
    assertNull(sym);

    table.addsym("i", "variable-local");
    assertNotNull("get i variable", table.getsymFromCurrentScope("i"));
    assertNotNull("get i variable", table.getsym("i"));

    table.pushscope(funcname);
    table.pushscope(funcname);
    table.pushscope(funcname);
    table.pushscope(funcname);
    table.pushscope(funcname);

    //table.dump();

    //    System.out.println("POP....");

    table.popscope();
    table.popscope();
    table.popscope();

    //table.dump();
  }

}
