package ast;

import java.io.IOException;

import jscan.Tokenlist;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast._typesnew.CType;
import ast.expr.main.CExpression;
import ast.expr.sem.TaStage;
import ast.expr.sem.TypeApplier;
import ast.parse.Parse;
import ast.stmt.main.CStatementBase;
import ast.unit.BlockItem;
import ast.unit.TranslationUnit;

public class _TestInternalConversions3 {

  @Test
  public void testConv3() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {           \n");
    sb.append(" /*002*/      int a[7];          \n");
    sb.append(" /*003*/      int b[1][3];       \n");
    sb.append(" /*004*/      void c(void);      \n");
    sb.append(" /*005*/      void (*d)(void);   \n");
    sb.append(" /*006*/      //                 \n");
    sb.append(" /*007*/    a       ;            \n");
    sb.append(" /*008*/    &a      ;            \n");
    sb.append(" /*009*/    *a      ;            \n");
    sb.append(" /*010*/    //                   \n");
    sb.append(" /*011*/    a+1     ;            \n");
    sb.append(" /*012*/    *(a+1)  ;            \n");
    sb.append(" /*013*/    &*(a+1) ;            \n");
    sb.append(" /*014*/    //                   \n");
    sb.append(" /*015*/    b       ;            \n");
    sb.append(" /*016*/    &b      ;            \n");
    sb.append(" /*017*/    *b      ;            \n");
    sb.append(" /*018*/    //                   \n");
    sb.append(" /*019*/    c       ;            \n");
    sb.append(" /*020*/    &c      ;            \n");
    sb.append(" /*021*/    *c      ;            \n");
    sb.append(" /*022*/    //                   \n");
    sb.append(" /*023*/    d       ;            \n");
    sb.append(" /*024*/    &d      ;            \n");
    sb.append(" /*025*/    *d      ;            \n");
    sb.append(" /*026*/    //                   \n");
    sb.append(" /*027*/      return 0;          \n");
    sb.append(" /*028*/  }                      \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();

    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    // typeinfo GCC
    // a : A7_i
    // &a : PA7_i
    // *a : i
    // a+1 : Pi
    // *(a+1) : i
    // &*(a+1) : Pi
    // b : A1_A3_i
    // &b : PA1_A3_i
    // *b : A3_i
    // c : FvvE
    // &c : PFvvE
    // *c : FvvE
    // d : PFvvE
    // &d : PPFvvE
    // *d : FvvE

    for (BlockItem block : unit.getExternalDeclarations().get(0).getFunctionDefinition().getCompoundStatement()
        .getCompound()) {
      if (block.getStatement() != null) {
        if (block.getStatement().getBase() == CStatementBase.SEXPR) {
          final CExpression expr = block.getStatement().getExpr();
          TypeApplier.applytype(expr, TaStage.stage_start);
          final CType resultType = expr.getResultType();
          //System.out.println(resultType.toString() + " : " + expr.toString());
          //System.out.println();
        }
      }
    }

  }

}
