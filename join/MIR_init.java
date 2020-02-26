package ast.join;

import static jscan.tokenize.T.TOKEN_NUMBER;

import java.util.ArrayList;
import java.util.List;

import jscan.cstrtox.NumType;
import jscan.sourceloc.SourceLocation;
import jscan.tokenize.Token;
import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast._typesnew.CTypeImpl;
import ast._typesnew.util.TypeUtil;
import ast.declarations.inits.Initializer;
import ast.declarations.inits.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.parse.NullChecker;
import ast.symtabg.elements.NumericConstant;

public class MIR_init {

  private final SourceLocation startLocation;
  private final CType type;
  private final List<InitNew> inits;
  private final Arrinfo arrinfo;
  private int level;

  public MIR_init(CType type, Initializer initializer) {
    NullChecker.check(type, initializer);

    this.startLocation = initializer.getLocation();
    this.type = type;
    this.inits = new ArrayList<InitNew>();
    this.arrinfo = new Arrinfo(type);
    this.level = -1;

    expand(type, initializer);
  }

  private void expand(CType type, Initializer initializer) {
    if (type.isArray()) {
      expandArrayInitializers(initializer);
      alignMultidimensionalArrayTail();
      applyOffsetToResult();
      applyArrayLengthAndTypeSize();
    }

    else if (type.isStrUnion()) {
      throw new ParseException("unimpl. struct-union initializers.");
    }

    else {
      final InitNew simpleInit = new InitNew(initializer.getAssignment());
      simpleInit.setOffset(0);
      inits.add(simpleInit);
    }
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

    int alignLen = TypeUtil.align(initsLen, actualLen);
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

  private void applyOffsetToResult() {
    int offset = 0;
    for (InitNew init : inits) {
      init.setOffset(offset);
      offset += arrinfo.getOneElementOffset();
    }
  }

  private void expandArrayInitializers(Initializer initializer) {
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
            Initializer initializerZero = new Initializer(initializerListZero, startLocation);
            initializers.add(new InitializerListEntry(initializerZero, startLocation));
          }
        }
      }

      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        expandArrayInitializers(entry.getInitializer());
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
    return new InitializerListEntry(new Initializer(zero(), startLocation), startLocation);
  }

  private CExpression digitZero() {
    Token from = new Token();
    from.set(TOKEN_NUMBER, "0");
    from.setLocation(startLocation);

    NumericConstant number = new NumericConstant(0, NumType.N_INT);
    CExpression ret = new CExpression(number, from);

    ret.setResultType(CTypeImpl.TYPE_INT);
    return ret;
  }

  private CExpression zero() {
    return digitZero();
  }

  public List<InitNew> getInits() {
    return inits;
  }

}
