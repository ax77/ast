package ast.join;

import java.util.ArrayList;
import java.util.List;

import jscan.tokenize.Token;
import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast.declarations.Initializer;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.symtabg.elements.CSymbol;

public class MIR_init {

  private final CSymbol symbol;
  private final Token from;
  private final List<InitNew> inits;
  private final Arrinfo arrinfo;

  private int level;

  public MIR_init(CSymbol symbol) {
    this.symbol = symbol;
    this.from = symbol.getFrom();
    this.inits = new ArrayList<InitNew>();
    this.arrinfo = new Arrinfo(symbol);
    this.level = -1;

    checkSymbol();
    expandInitializers(symbol.getInitializer());
    alignMultidimensionalArrayTail();
    applyOffsetToResult();
    applyArrayLengthAndTypeSize();
  }

  // inside array all elements already filling with zeros
  // alignment need to count real size of first dimension from initializer-list
  //
  private void alignMultidimensionalArrayTail() {
    if (!arrinfo.isMultiDimensionalArray()) {
      return;
    }

    List<Integer> dimscopy = arrinfo.getArrayDimensions();
    dimscopy.remove(0);

    int actualLen = getKnownLenOfMultidimensionalArray();
    int initsLen = inits.size();

    int alignLen = TypesUtil.align(initsLen, actualLen);
    int alignDiff = alignLen - initsLen;

    for (int i = 0; i < alignDiff; i++) {
      inits.add(new InitNew(zero()));
    }
  }

  private int getKnownLenOfMultidimensionalArray() {
    List<Integer> dimscopy = arrinfo.getArrayDimensions();
    dimscopy.remove(0);

    // get known length of array, exclude first dimension
    int actualLen = 1;
    for (Integer i : dimscopy) {
      actualLen *= i.intValue();
    }

    return actualLen;
  }

  // TODO: check inits overflow if size are specified.

  private void applyArrayLengthAndTypeSize() {
    if (!arrinfo.isZeroFirstDimension()) {
      return;
    }

    CType type = symbol.getType();
    CArrayType array = type.getTpArray();

    if (!arrinfo.isMultiDimensionalArray()) {
      int arrlen = inits.size();
      array.setArrayLen(arrlen);
      type.setSize(array.getArrayLen() * array.getArrayOf().getSize());
    }

    else {
      int actual = getKnownLenOfMultidimensionalArray();
      int arrlen = inits.size() / actual;
      array.setArrayLen(arrlen);
      type.setSize(array.getArrayLen() * array.getArrayOf().getSize());
    }
  }

  private void symErr(String m) {
    throw new ParseException(symbol.getLocationToString() + ": error: " + m);
  }

  private void checkSymbol() {
    if (!symbol.isArray()) {
      symErr("expect array for this initializer");
    }
    if (symbol.getInitializer() == null) {
      symErr("no initializer");
    }
  }

  private void applyOffsetToResult() {
    int offset = 0;
    for (InitNew init : inits) {
      init.setOffset(offset);
      offset += arrinfo.getOneElementOffset();
    }
  }

  private void expandInitializers(Initializer initializer) {
    if (!initializer.isInitializerList()) {
      InitNew init = new InitNew(initializer.getAssignment());
      inits.add(init);
    }

    else {

      ++level;

      List<InitializerListEntry> initializers = initializer.getInitializerList();

      // fill zero in fully-braced init-lists
      // int arr_03[1][2][3] = { {{1    } ,  {4,5,6,}} };
      // ...........................^..^

      if (!isHasNested(initializers)) {
        int expected = arrinfo.getElemsPerBlockAtLevel().get(level).intValue();
        int actual = initializers.size();

        if (expected <= 0) {
          expected = actual;
          //throw new ParseException("zero or negative array length. unsupported now.");
        }

        if (actual > expected) {
          throw new ParseException("inits {} overflow. unsupp.");
        }

        int diff = expected - actual;
        for (int i = 0; i < diff; i++) {
          initializers.add(zeroInit());
        }

        //System.out.printf("level=%d, list=%s, arrlen=%d\n", level, initializers, expected);
      }

      else {
        // XXX: add initializer-list-here-with-zeros!!!
        //

        if (isOnlyNesteds(initializers)) {
          int expected = arrinfo.getArrayDimensions().get(level).intValue();
          int actual = initializers.size();

          if (expected <= 0) {
            expected = actual;
            //throw new ParseException("zero or negative array length. unsupported now.");
          }

          if (actual > expected) {
            throw new ParseException("inits {} overflow. unsupp.");
          }

          int diff = expected - actual;
          for (int i = 0; i < diff; i++) {
            List<InitializerListEntry> initializerListZero = new ArrayList<InitializerListEntry>();
            Initializer initializerZero = new Initializer(initializerListZero);
            initializers.add(new InitializerListEntry(initializerZero));
          }
        }
      }

      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        expandInitializers(entry.getInitializer());
      }

      --level;

    }
  }

  private boolean isHasNested(List<InitializerListEntry> initializers) {
    for (int j = 0; j < initializers.size(); j++) {
      InitializerListEntry entry = initializers.get(j);
      if (entry.getInitializer().isInitializerList()) {
        return true;
      }
    }
    return false;
  }

  private boolean isOnlyNesteds(List<InitializerListEntry> initializers) {
    for (int j = 0; j < initializers.size(); j++) {
      InitializerListEntry entry = initializers.get(j);
      if (!entry.getInitializer().isInitializerList()) {
        return false;
      }
    }
    return true;
  }

  private InitializerListEntry zeroInit() {
    return new InitializerListEntry(new Initializer(zero()));
  }

  private CExpression zero() {
    return CExpressionBuilderHelper.digitZero(from);
  }

  public List<InitNew> getInits() {
    return inits;
  }

}
