package ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jscan.Tokenlist;
import jscan.preprocess.ScanExc;

import org.junit.Ignore;
import org.junit.Test;

import ast._entry.PreprocessSourceForParser;
import ast._entry.PreprocessSourceForParserVariant;
import ast._typesnew.CType;
import ast.declarations.Initializer;
import ast.declarations.InitializerList;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.unit.TranslationUnit;

class ArrayDimensionsExpander {

  // TODO: location
  // TODO: symbol instead of type?

  private final CType type;
  private final List<Integer> dims;
  private final List<Integer> esiz;
  private final int fullArrayLen;
  private final int elementSize;

  // given array: int x[2][2]
  // dims = [2, 2]
  // element_size = sizeof(int)
  // fullArrayLen = (2*2) = 4

  /*
  int arr[2][2] = {1,2,3,4};
  // ::
  mov     DWORD PTR [rbp-16], 1
  mov     DWORD PTR [rbp-12], 2
  mov     DWORD PTR [rbp-8 ], 3
  mov     DWORD PTR [rbp-4 ], 4
  */

  public ArrayDimensionsExpander(CType type) {
    NullChecker.check(type);

    if (!type.isArray()) {
      throw new ParseException("internal error: " + "expected array for dimension expander");
    }

    this.type = type;
    this.dims = new ArrayList<Integer>(0);
    this.esiz = new ArrayList<Integer>(0);

    buildArrayDimensions(this.type, this.dims, this.esiz);

    if (esiz.isEmpty() || esiz.size() != 1) {
      throw new ParseException("internal error: " + "unknown element size");
    }

    if (dims.isEmpty()) {
      throw new ParseException("internal error: " + "empty array dimensions");
    }

    if (dims.get(0).intValue() == 0) {
      throw new ParseException("internal error: " + "computed array len unimplimented");
    }

    // esize
    this.elementSize = esiz.get(0).intValue();

    // full-len
    int len = 1;
    for (Integer i : dims) {
      len *= i.intValue();
    }
    this.fullArrayLen = len;
  }

  private void buildArrayDimensions(CType typeGiven, List<Integer> dimsOut, List<Integer> esizOut) {
    if (!typeGiven.isArray()) {
      esizOut.add(typeGiven.getSize());
      return;
    }
    dimsOut.add(typeGiven.getTpArray().getArrayLen());
    buildArrayDimensions(typeGiven.getTpArray().getArrayOf(), dimsOut, esizOut);
  }

  public List<Integer> getDims() {
    return dims;
  }

  public int getFullArrayLen() {
    return fullArrayLen;
  }

  public int getElementSize() {
    return elementSize;
  }

}

class OffsetEntry {

  private final List<Integer> index;
  private final CExpression expression;

  public OffsetEntry(List<Integer> index, CExpression expression) {
    this.index = index;
    this.expression = expression;
  }

  public List<Integer> getIndex() {
    return index;
  }

  public CExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    return index.toString() + ":" + expression.toString();
  }

}

class Offset {

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

  private void createInitExpressionsFromBracedInitializers() {

    ArrayDimensionsExpander exp = new ArrayDimensionsExpander(symbol.getType());
    List<Integer> dims = exp.getDims();

    for (Integer I : dims) {
      for (int j = 0; j < I; j++) {
        OffsetEntry ent = entries.remove(0);
        //System.out.println(ent);

        // int arr2[2][2] = {1,2,3,4};
        // [0]:1
        // [1]:2
        // [2]:3
        // [3]:4

        // int arr1[2][2] = {{1,2}, {3,4}};
        // [0, 0]:1
        // [0, 1]:2
        // [1, 0]:3
        // [1, 1]:4
      }
    }

  }

  private void buildIndices(Initializer initializer) {
    if (!initializer.isHasInitializerList()) {
      OffsetEntry entry = new OffsetEntry(initializer.getIndex(), initializer.getAssignment());
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

public class _TestArrayInitializers {

  @Test
  public void testFirst() throws IOException {
    //@formatter:off
    StringBuilder sb = new StringBuilder();
    sb.append(" /*001*/  void f() {                           \n");
//    sb.append(" /*002*/    int arr1[2][2] = {{1,2}, {3,4}};   \n");
    sb.append(" /*003*/    int arr2[2][2] = {1,2,3,4};        \n");
    sb.append(" /*004*/  }                                    \n");
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

}
