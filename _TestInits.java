package ast;

import static jscan.tokenize.T.TOKEN_NUMBER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;
import jscan.cstrtox.NumType;
import jscan.preprocess.ScanExc;
import jscan.tokenize.T;
import jscan.tokenize.Token;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast._typesnew.CTypeImpl;
import ast.declarations.Initializer;
import ast.declarations.InitializerList;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilder;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.NumericConstant;
import ast.unit.TranslationUnit;

class OffsetEntry {

  private final CSymbol symbol;
  private final List<Integer> index;
  private final CExpression expression;

  public OffsetEntry(CSymbol symbol, List<Integer> index, CExpression expression) {
    this.symbol = symbol;
    this.index = index;
    this.expression = expression;
  }

  public List<Integer> getIndex() {
    return index;
  }

  public CExpression getExpression() {
    return expression;
  }

}

//#include <stdio.h>
//int main() {
//   
//    int arr1[2][2] = { {1,2}, {3,4} };
//    
//    int arr2[2][2];
//    arr2[0][0] = 1;
//    arr2[0][1] = 2;
//    arr2[1][0] = 3;
//    arr2[1][1] = 4;
//    
//    int arr3[2][2];
//    *((*(arr3+0))+0) = 1;
//    *((*(arr3+0))+1) = 2;
//    *((*(arr3+1))+0) = 3;
//    *((*(arr3+1))+1) = 4;
//    
//    int arr[2][2];
//    ((*((&(*(int *) ((*((&(*(arr + 0))) + 0)) + 0))) + 0)) = (int) 1);
//    ((*((&(*(int *) ((*((&(*(arr + 0))) + 0)) + 0))) + 1)) = (int) 2);
//    ((*((&(*(int *) ((*((&(*(arr + 0))) + 1)) + 0))) + 0)) = (int) 3);
//    ((*((&(*(int *) ((*((&(*(arr + 0))) + 1)) + 0))) + 1)) = (int) 4);
//    
//    //1)
//    //build this indexes:
//    //[0,0] = 1
//    //[0,1] = 2
//    //[1,0] = 3
//    //[1,1] = 4
//    
//    for(int i=0; i<2; i++) {
//        for(int j=0; j<2; j++) {
//            //printf("[%d,%d]=%d\n", i, j, arr[i][j]);
//        }
//    }
//    
//    int a4[2][2][2]; // = { {{1, 2}, {3, 4}}, {{5, 6}, {7, 8}} };
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 0)) + 0))) + 0)) + 0))) + 0)) = 1);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 0)) + 0))) + 0)) + 0))) + 1)) = 2);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 0)) + 0))) + 1)) + 0))) + 0)) = 3);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 0)) + 0))) + 1)) + 0))) + 1)) = 4);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 1)) + 0))) + 0)) + 0))) + 0)) = 5);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 1)) + 0))) + 0)) + 0))) + 1)) = 6);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 1)) + 0))) + 1)) + 0))) + 0)) = 7);
//    ((*((&(*((*((&(*((*((&(*(a4 + 0))) + 1)) + 0))) + 1)) + 0))) + 1)) = 8);
//    
//    for(int i=0; i<2; i++) {
//        for(int j=0; j<2; j++) {
//            for(int k=0; k<2; k++) {
//                printf("[%d,%d,%d]=%d\n", i, j, k, a4[i][j][k]);
//            }
//        }
//    }
//    
//    return 0;
//}

class Offset {

  // TODO: simplify deref
  // simplify numeric

  // build from this
  private final CSymbol symbol;
  private final Initializer initializer;

  // temporary
  private List<OffsetEntry> entries;

  // out
  private List<CExpression> initExpressions;

  public Offset(CSymbol symbol, Initializer initializer) {
    NullChecker.check(symbol, initializer);
    if (!symbol.isArray()) {
      throw new ParseException("expect array for this initializer, but was: " + symbol.toString());
    }

    this.symbol = symbol;
    this.initializer = initializer;
    this.entries = new ArrayList<OffsetEntry>(0);
    this.initExpressions = new ArrayList<CExpression>(0);

    buildIndices(this.initializer);
    createInitExpressionsFromBracedInitializers();
  }

  private CExpression createNumericConst(Token from, Integer intValue) {

    final NumericConstant number = new NumericConstant(intValue.intValue(), NumType.N_INT);
    final Token tokenNumeric = CExpressionBuilderHelper.copyTokenAddNewType(from, TOKEN_NUMBER, intValue.toString());

    final CExpression numericConstant = new CExpression(number, tokenNumeric);
    numericConstant.setResultType(CTypeImpl.TYPE_INT);

    return numericConstant;
  }

  private void createInitExpressionsFromBracedInitializers() {
    // *((*(arr3+0))+0) = 1;
    // *((*(arr3+0))+1) = 2;

    // arr2[0][0] = 1;
    // arr2[0][1] = 2;

    final Token from = symbol.getFrom();
    Token plusOperator = CExpressionBuilderHelper.copyTokenAddNewType(from, T.T_PLUS, "+"); // TODO:XXX:special builders for this useful tokens
    Token derefOperator = CExpressionBuilderHelper.copyTokenAddNewType(from, T.T_TIMES, "*"); // TODO:XXX:special builders for this useful tokens
    Token assignOperator = CExpressionBuilderHelper.copyTokenAddNewType(from, T.T_ASSIGN, "="); // TODO:XXX:special builders for this useful tokens

    // TODO: hard check, what the type is initialized,
    // what expression we have, etc...

    for (OffsetEntry ent : entries) {
      List<Integer> indexes = ent.getIndex();
      if (indexes.isEmpty()) {
        throw new ParseException("empty initializer...");
      }
      Integer first = indexes.remove(0);

      // *(arr3+0)
      // all other: rest of, just additions.

      // (arr3+0)
      //
      final CExpression eSymbol = CExpressionBuilder.esymbol(symbol, from); // TODO:XXX:parser here does't need
      CExpression eAddition = CExpressionBuilder.binary(plusOperator, eSymbol, createNumericConst(from, first)); // TODO:XXX:parser here does't need

      CExpression eDereference = CExpressionBuilder.unary(derefOperator, eAddition);

      // rest
      for (Integer i : indexes) {
        // *((*(arr3+0))+0)
        // .............^

        final CExpression numericConstantRest = createNumericConst(from, i);
        CExpression additionRest = CExpressionBuilder.binary(plusOperator, eDereference, numericConstantRest);// TODO:XXX:parser here does't need

        // apply to deref this addition
        // 
        eDereference = CExpressionBuilder.unary(derefOperator, additionRest);
      }

      // assign to this computation real initializer expression.
      //
      CExpression assignedInitializer = CExpressionBuilder.assign(assignOperator, eDereference, ent.getExpression());
      initExpressions.add(assignedInitializer);
    }
  }

  private void buildIndices(Initializer initializer) {
    if (!initializer.isHasInitializerList()) {
      OffsetEntry entry = new OffsetEntry(symbol, initializer.getIndex(), initializer.getAssignment());
      entries.add(entry);
    } else {
      final InitializerList initializerList = initializer.getInitializerList();
      final List<InitializerListEntry> initializers = initializerList.getInitializers();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        if (entry.isDesignation()) {
          throw new ScanExc("unsupported now");
        }
        Initializer inittmp = entry.getInitializer();
        inittmp.pushall(initializer.getIndex());
        inittmp.pushi(j);
        buildIndices(inittmp);
      }
    }
  }

  public void show() {
    System.out.println(symbol.getName().getName() + ":");
    for (CExpression e : initExpressions) {
      System.out.println(e.toString());
    }
  }

}

public class _TestInits {

  @Test
  public void testFirst() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {              \n");
    sb.append(" /*002*/      int arr[2][2] = {     \n");
    sb.append(" /*003*/          {1,2}, {3,4}      \n");
    sb.append(" /*004*/      };                    \n");
    sb.append(" /*005*/      int a4[2][2][2] = { {{1, 2}, {3, 4}}, {{5, 6}, {7, 8}} };   \n");
    sb.append(" /*005*/      return sizeof(arr);   \n");
    sb.append(" /*006*/  }                         \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

    final List<CSymbol> locals = unit.getExternalDeclarations().get(0).getFunctionDefinition().getLocals();
    for (CSymbol sym : locals) {
      if (sym.getName().getName().equals("__func__")) {
        continue;
      }
      if (sym.getInitializer() == null) {
        continue;
      }
      final Initializer initializer = sym.getInitializer();
      Offset offsetBuilder = new Offset(sym, initializer);
      //offsetBuilder.show();
    }
  }

  @Ignore
  @Test
  public void testArraysSimple() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                                                   \n");
    sb.append(" /*002*/      int a1[2][3] = {{1, 3, 0}, {-1, 5, 9}};                    \n");
    sb.append(" /*003*/      int a2[][3] = {{1, 3, 0}, {-1, 5, 9}};                     \n");
    sb.append(" /*004*/      int a3[2][3] = {1, 3, 0, -1, 5, 9};                        \n");
    sb.append(" /*005*/      int a4[][2][2] = { {{1, 2}, {3, 4}}, {{5, 6}, {7, 8}} };   \n");
    sb.append(" /*006*/      int a5[1][2][3][4][5] = { 0 };                             \n");
    sb.append(" /*007*/      return 0;                                                  \n");
    sb.append(" /*008*/  }                                                              \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

  @Ignore
  @Test
  public void testDesignations1() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                    \n");
    sb.append(" /*002*/      struct s {                  \n");
    sb.append(" /*003*/          int type;               \n");
    sb.append(" /*004*/          struct ptr {            \n");
    sb.append(" /*005*/              int flag;           \n");
    sb.append(" /*006*/          } *ptr;                 \n");
    sb.append(" /*007*/      } opts = {                  \n");
    sb.append(" /*008*/          .type = 0,              \n");
    sb.append(" /*009*/          .ptr = &(struct ptr){   \n");
    sb.append(" /*010*/              .flag = -1,         \n");
    sb.append(" /*011*/          },                      \n");
    sb.append(" /*012*/      };                          \n");
    sb.append(" /*013*/      return 0;                   \n");
    sb.append(" /*014*/  }                               \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

  @Ignore
  @Test
  public void testDesignations2() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                    \n");
    sb.append(" /*002*/      struct A { int x, y; };     \n");
    sb.append(" /*003*/      struct B { struct A a; };   \n");
    sb.append(" /*004*/      struct B b = {.a.x = 0};    \n");
    sb.append(" /*005*/      return 0;                   \n");
    sb.append(" /*006*/  }                               \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

  @Ignore
  @Test
  public void testDesignations3() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  typedef struct { int k; int l; int a[2]; } T;                       \n");
    sb.append(" /*002*/  typedef struct { int i;  T t; } S;                                  \n");
    sb.append(" /*003*/  T x = {.l = 43, .k = 42, .a[1] = 19, .a[0] = 18 };                  \n");
    sb.append(" /*004*/   // x initialized to {42, 43, {18, 19} }                            \n");
    sb.append(" /*005*/  int main()                                                          \n");
    sb.append(" /*006*/  {                                                                   \n");
    sb.append(" /*007*/      S l = { 1,          // initializes l.i to 1                     \n");
    sb.append(" /*008*/             .t = x,      // initializes l.t to {42, 43, {18, 19} }   \n");
    sb.append(" /*009*/             .t.l = 41,   // changes l.t to {42, 41, {18, 19} }       \n");
    sb.append(" /*010*/             .t.a[1] = 17 // changes l.t to {42, 41, {18, 17} }       \n");
    sb.append(" /*011*/            };                                                        \n");
    sb.append(" /*012*/      // .t = x sets l.t.k to 42 explicitly                           \n");
    sb.append(" /*013*/      // .t.l = 42 would zero out l.t.k implicitly                    \n");
    sb.append(" /*014*/  }                                                                   \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

  @Ignore
  @Test
  public void testDesignations4() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  int main() {                        \n");
    sb.append(" /*002*/      struct A {                      \n");
    sb.append(" /*003*/          int flag;                   \n");
    sb.append(" /*004*/          struct opts {               \n");
    sb.append(" /*005*/              int cmd;                \n");
    sb.append(" /*006*/              long long i64, *pi64;   \n");
    sb.append(" /*007*/              char *cstring;          \n");
    sb.append(" /*008*/              struct debugopt {       \n");
    sb.append(" /*009*/                  int test;           \n");
    sb.append(" /*010*/                  char padding;       \n");
    sb.append(" /*011*/              } debugopt;             \n");
    sb.append(" /*012*/          } opt;                      \n");
    sb.append(" /*013*/          long nodebug;               \n");
    sb.append(" /*014*/          int third[3];               \n");
    sb.append(" /*015*/      } varname = {                   \n");
    sb.append(" /*016*/          .flag = -1,                 \n");
    sb.append(" /*017*/          .opt = {                    \n");
    sb.append(" /*018*/              .cmd = -2,              \n");
    sb.append(" /*019*/              .i64 = -3,              \n");
    sb.append(" /*020*/              .debugopt = {           \n");
    sb.append(" /*021*/                  .test = -4,         \n");
    sb.append(" /*022*/                  .padding = \' \'    \n");
    sb.append(" /*023*/              },                      \n");
    sb.append(" /*024*/          },                          \n");
    sb.append(" /*025*/          .nodebug = 32768,           \n");
    sb.append(" /*026*/          .third[0] = 2048,           \n");
    sb.append(" /*027*/          .third[1] = 4096,           \n");
    sb.append(" /*028*/          .third[2] = 8192,           \n");
    sb.append(" /*029*/      };                              \n");
    sb.append(" /*030*/      return 0;                       \n");
    sb.append(" /*031*/  }                                   \n");
    //@formatter:on

    Tokenlist it = new PreprocessSourceForParser(new PreprocessSourceForParserVariant(sb.toString(), false)).pp();
    Parse p = new Parse(it);
    TranslationUnit unit = p.parse_unit();

  }

}
