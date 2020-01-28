package ast.arrayinit;

import java.util.ArrayList;
import java.util.List;

import jscan.preprocess.ScanExc;
import ast.declarations.Initializer;
import ast.declarations.InitializerList;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.symtabg.elements.CSymbol;

public class OffsetInitializer {

  // build from this
  private final CSymbol symbol;
  private final Initializer initializer;

  // temporary
  private List<OffsetInitializerEntry> entries;

  // out
  private List<CExpression> initExpressions;

  public OffsetInitializer(CSymbol symbol, Initializer initializer) {
    NullChecker.check(symbol, initializer);
    if (!symbol.isArray()) {
      throw new ParseException("expect array for this initializer, but was: " + symbol.toString());
    }

    this.symbol = symbol;
    this.initializer = initializer;
    this.entries = new ArrayList<OffsetInitializerEntry>(0);
    this.initExpressions = new ArrayList<CExpression>(0);

    buildIndices(this.initializer);
    createInitExpressionsFromBracedInitializers();
  }

  private void createInitExpressionsFromBracedInitializers() {

    ArrayDimensionsExpander exp = new ArrayDimensionsExpander(symbol.getType());
    List<Integer> dims = exp.getDims();
    List<Integer> esiz = exp.getEsiz();

    // array_element_offset         = ((indexN * arrsizeN) + (indexN... * arrsizeN...))
    // array_element_index_flat     = array_element_offset / elem_size
    //
    // int arr[2][2][2];
    // int w = sizeof(arr);          // 32
    // int x = sizeof(arr[0]);       // 16
    // int y = sizeof(arr[0][0]);    // 8
    // int z = sizeof(arr[0][0][0]); // 4
    //
    // arr[1][1][1] = (1*16) + (1*8) + (1*4) = 16+8+4 -> (offset=28) :: 28/sizeof(int) -> (index=7)
    //
    //
    // check:
    //
    //    | index        | offset of base
    // 0) | arr[0][0][0] |  0
    // 1) | arr[0][0][1] |  4
    // 2) | arr[0][1][0] |  8
    // 3) | arr[0][1][1] | 12
    // 4) | arr[1][0][0] | 16
    // 5) | arr[1][0][1] | 20
    // 6) | arr[1][1][0] | 24
    // 7) | arr[1][1][1] | 28

    // need:
    // InitOffset  = InitializerExpression
    //

    int oneElementOffset = esiz.get(esiz.size() - 1);

    if (exp.getFullArrayLen() == entries.size()) {

      // that's ok: for each element present special initializer.
      // start with this.

      int initializerOffset = 0;
      for (int x = 0; x < dims.size(); x++) {
        while (!entries.isEmpty()) {
          OffsetInitializerEntry ent = entries.remove(0);
          System.out.printf("%-3d = %s\n", initializerOffset, ent.getExpression().toString());
          initializerOffset += oneElementOffset;
        }
      }

    }

    else {

      while (!entries.isEmpty()) {
        OffsetInitializerEntry ent = entries.remove(0);

        // fill with zero if unbraced nested:
        // int arr2[2][2][2] = { {1}, {{5, 6}, {7, 8}} };
        // 
        // THIS:
        //    [0, 0]:1
        //    [1, 0, 0]:5
        //    [1, 0, 1]:6
        //    [1, 1, 0]:7
        //    [1, 1, 1]:8
        // TO THAT:
        //    [0, 0, 0]:1
        //    [1, 0, 0]:5
        //    [1, 0, 1]:6
        //    [1, 1, 0]:7
        //    [1, 1, 1]:8

        List<Integer> index = ent.getIndex();
        for (int i = index.size(); i < dims.size(); i++) {
          index.add(0);
        }

        int initializerOffset = 0;
        for (int x = 0; x < index.size(); x++) {
          int i = index.get(x);
          int s = esiz.get(x);
          initializerOffset += (i * s);
        }
        System.out.printf("%-3d = %s\n", initializerOffset, ent.getExpression().toString());

      }

    }

  }

  private void buildIndices(Initializer initializer) {
    if (!initializer.isHasInitializerList()) {
      OffsetInitializerEntry entry = new OffsetInitializerEntry(initializer.getIndex(), initializer.getAssignment());
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