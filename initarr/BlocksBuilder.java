package ast.initarr;

import java.util.ArrayList;
import java.util.List;

import jscan.preprocess.ScanExc;
import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast.declarations.Initializer;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.main.CExpression;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.symtabg.elements.CSymbol;

class OffsetInitializerEntry {

  private int level;
  private CExpression expression;

  public OffsetInitializerEntry(int level, CExpression expression) {
    this.level = level;
    this.expression = expression;
  }

  public int getLevel() {
    return level;
  }

  public CExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    return expression.toString();// String.format("%-3d", weight) + " = " + expression;
  }

}

class InitBlock {
  private List<OffsetInitializerEntry> fromBlocks;
  private List<OffsetInitializerEntry> wildEntries;

  public InitBlock() {
    this.fromBlocks = new ArrayList<OffsetInitializerEntry>(0);
    this.wildEntries = new ArrayList<OffsetInitializerEntry>(0);
  }

  public void pushToBlocks(OffsetInitializerEntry e) {
    fromBlocks.add(e);
  }

  public void pushToWild(OffsetInitializerEntry e) {
    wildEntries.add(e);
  }

  public boolean isWildAvail(int maxlen) {
    return wildEntries.size() < maxlen;
  }

  @Override
  public String toString() {
    return "B=" + fromBlocks + "; W=" + wildEntries;
  }

  public List<OffsetInitializerEntry> getFromBlocks() {
    return fromBlocks;
  }

  public List<OffsetInitializerEntry> getWildEntries() {
    return wildEntries;
  }

}

class Blocks {

  // int arr[][2] = { {1}, {2}, 3,4,5,6,7 };
  // mdeep == 1 : the count of array's, except first 
  // mlen  == 2 

  // int arr[][2][2] = { {{1,2},3,4},{5},6 }; 
  // mdeep == 2
  // mlen  == 2*2

  private final CSymbol symbol;
  private final int mdeep;
  private final int mlen;
  private final List<InitBlock> blocks;

  private int initsCount;
  private final List<Integer> dimensions;

  private final int oneElementOffset; // XXX: for int arr[][2][3] is INT_SIZE
  private boolean lengthComeFromInitializersCount;

  private final List<List<InitNew>> fixupBlocks;

  public Blocks(CSymbol sym) {
    this.symbol = sym;

    if (!sym.isArray()) {
      err("expect array for this initializer.");
    }
    List<Integer> sizeOut = new ArrayList<Integer>(0);
    this.dimensions = new ArrayList<Integer>(0);
    buildArrayDimensions(sym.getType(), dimensions, sizeOut);

    int mlenTmp = 0;

    // int x[][][]
    if (dimensions.size() > 1) {
      mlenTmp = 1;
      for (int x = 1; x < dimensions.size(); x++) {
        int i = dimensions.get(x).intValue();
        if (i == 0) {
          err("zero array len");
        }
        mlenTmp *= i;
      }
    }

    // int x[]
    else if (dimensions.size() == 1) {
      if (dimensions.get(0) != 0) {
        mlenTmp = dimensions.get(0);
      } else {
        lengthComeFromInitializersCount = true;
      }
    }

    // ???
    else {
      err("error with array.");
    }

    if (sizeOut.size() != 1) {
      err("unknown size...");
    }
    oneElementOffset = sizeOut.get(0);

    this.mdeep = dimensions.size();
    this.mlen = mlenTmp;
    this.blocks = new ArrayList<InitBlock>(0);
    this.fixupBlocks = new ArrayList<List<InitNew>>(0);
  }

  private void addToFixup(InitNew e) {
    boolean fixupAvail = !fixupBlocks.isEmpty() && fixupBlocks.get(fixupBlocks.size() - 1).size() < mlen;
    if (!fixupAvail) {
      fixupBlocks.add(new ArrayList<InitNew>(0));
    }
    fixupBlocks.get(fixupBlocks.size() - 1).add(e);
  }

  private void buildArrayDimensions(CType typeGiven, List<Integer> dimensions, List<Integer> sizeOut) {
    if (!typeGiven.isArray()) {
      sizeOut.add(typeGiven.getSize());
      return;
    }
    dimensions.add(typeGiven.getTpArray().getArrayLen());
    buildArrayDimensions(typeGiven.getTpArray().getArrayOf(), dimensions, sizeOut);
  }

  private void err(String m) {
    throw new ParseException(symbol.getLocationToString() + " error: " + m);
  }

  public List<InitBlock> getBlocks() {
    return blocks;
  }

  public int getMdeep() {
    return mdeep;
  }

  public int getMlen() {
    return mlen;
  }

  public List<InitNew> getMergingResult() {
    List<InitNew> mergingResult = new ArrayList<InitNew>();
    for (List<InitNew> e : fixupBlocks) {
      mergingResult.addAll(e);
    }
    int offset = 0;
    for (InitNew e : mergingResult) {
      e.setOffset(offset);
      offset += oneElementOffset;
    }
    return mergingResult;
  }

  public int getInitsCount() {
    return initsCount;
  }

  public void createEmptyWild() {
    blocks.add(new InitBlock());
  }

  public boolean isWildAvail(int maxlen) {
    return !blocks.isEmpty() && blocks.get(blocks.size() - 1).isWildAvail(maxlen);
  }

  public void pushToWild(OffsetInitializerEntry e) {
    if (!isWildAvail(mlen)) {
      createEmptyWild();
    }
    blocks.get(blocks.size() - 1).pushToWild(e);
  }

  public void pushToBlocks(List<OffsetInitializerEntry> entries) {
    blocks.add(new InitBlock());
    for (OffsetInitializerEntry e : entries) {
      blocks.get(blocks.size() - 1).pushToBlocks(e);
    }
  }

  public void pushToWild(List<OffsetInitializerEntry> entries) {
    for (OffsetInitializerEntry e : entries) {
      pushToWild(e);
    }
  }

  private void checkBounds() {
    for (InitBlock block : blocks) {
      final List<OffsetInitializerEntry> fromBlocks = block.getFromBlocks();
      final List<OffsetInitializerEntry> fromWild = block.getWildEntries();

      // I)
      if (!fromBlocks.isEmpty()) {
        if (fromBlocks.size() > mlen) {
          err("extra length within nested braced-initializers.");
        }

        int maxdeepActual = 0;
        for (OffsetInitializerEntry e : fromBlocks) {
          if (maxdeepActual < e.getLevel()) {
            maxdeepActual = e.getLevel();
          }
        }
        if (maxdeepActual > mdeep) {
          err("extra braces. deep too large.");
        }
      }

      // II)
      if (!fromWild.isEmpty()) {
        if (fromWild.size() > mlen && !lengthComeFromInitializersCount) {
          err("extra length.");
        }
      }

    }
  }

  // XXX: order of call this methods are important
  // TODO: more clean and readable
  public void merge() {
    checkBounds();
    buildResultList();
    fixall();
  }

  private void buildResultList() {
    for (InitBlock block : blocks) {
      final List<OffsetInitializerEntry> fromBlocks = block.getFromBlocks();
      final List<OffsetInitializerEntry> fromWild = block.getWildEntries();
      if (!fromBlocks.isEmpty()) {
        initsCount++;
      }
      if (!fromWild.isEmpty()) {
        initsCount++;
      }
      // I)
      for (OffsetInitializerEntry e : fromBlocks) {
        addToFixup(new InitNew(e.getExpression()));
      }
      if (!fromBlocks.isEmpty()) {
        for (int i = fromBlocks.size(); i < mlen; i++) {
          addToFixup(zero());
        }
      }
      // II)
      for (OffsetInitializerEntry e : fromWild) {
        addToFixup(new InitNew(e.getExpression()));
      }
      if (!fromWild.isEmpty()) {
        for (int i = fromWild.size(); i < mlen; i++) {
          addToFixup(zero());
        }
      }
    }
  }

  private InitNew zero() {
    return new InitNew(CExpressionBuilderHelper.digitZero(symbol.getFrom()));
  }

  /////
  private void finishArrayType(int withLen) {

    if (withLen <= 0) {
      err("zero or negative lenght...");
    }

    CType type = symbol.getType();
    CArrayType arr = type.getTpArray();

    if (arr.getArrayLen() != 0) {
      err("array length already known");
    }

    arr.setArrayLen(withLen);

    type.setSize(arr.getArrayLen() * arr.getArrayOf().getSize());
    type.setAlign(arr.getArrayOf().getAlign());
  }

  private void fixall() {

    //int arr[ ] = { 1,2,3 }; // the len == 3
    //int arr[5] = { 1,2,3 }; // the len == 5, with trailing zeros
    //int arr[ ] = {  };      // the len == 0, the size == 0 -> is an error

    final CArrayType arr = symbol.getType().getTpArray();
    final int arlen = arr.getArrayLen();
    final boolean isOneDimensionalArray = !arr.getArrayOf().isArray();

    int xxxxx = fixupBlocks.size(); // this line for debugger use

    if (isOneDimensionalArray) {

      if (arlen == 0) {
        // int x[ ] = { };
        // how use this object?
        //
        if (fixupBlocks.isEmpty()) {
          err("zero-sized array with empty initializer-list not allowed");
        }

        int newlen = fixupBlocks.size();
        finishArrayType(newlen);

      }

      else {
        if (fixupBlocks.isEmpty()) {
          // int x[5] = { };
          // fill all with zeros
          //
          fixupBlocks.add(new ArrayList<InitNew>(0));
          for (int j = 0; j < arlen; j++) {
            addToFixup(zero());
          }
        }

        else {

          if (fixupBlocks.size() > 1) {
            err(".0.fixed-sized array overflow");
          }

          // int x[5] = { 0 };
          // we fill trailing zeros when build blocks.
          // check here that all ok
          for (List<InitNew> e : fixupBlocks) {
            int inits = e.size();
            if (inits != arlen) {
              err(".1.fixed-sized array overflow");
            }
          }
        }
      }

    }

    else {

      if (arlen == 0) {

        if (fixupBlocks.isEmpty()) {
          err("zero-sized array with empty initializer-list not allowed");
        }

        // calc len without first
        //
        // int x[][3] = {{1, 3, 0}, {-1, 5, 9}};
        int restlen = 1;
        for (int i = 1; i < dimensions.size(); i++) {
          final Integer xx = dimensions.get(i);
          if (xx == 0) {
            err(".3.zero size for array may be in first dimension only.");
          }
          restlen *= xx;
        }

        for (List<InitNew> e : fixupBlocks) {
          if (e.size() > restlen) {
            err(".1.fixed-sized multidimensional array overflow");
          }
        }

        int newlen = fixupBlocks.size();
        finishArrayType(newlen);
      }

      else {

        // I)
        // calc len include first
        //
        // int c0[3][4] = {0,1,2,3,4,5,6,7,8,9,10,11};
        int lenExpected = 1;
        for (int i = 0; i < dimensions.size(); i++) {
          final Integer xx = dimensions.get(i);
          if (xx == 0) {
            err(".3.zero size for array may be in first dimension only.");
          }
          lenExpected *= xx;
        }

        // II)
        if (fixupBlocks.isEmpty()) {
          // short q1[4][3][2] = { { { } } };
          // fill all with zeros
          //
          fixupBlocks.add(new ArrayList<InitNew>(0));
          for (int j = 0; j < lenExpected; j++) {
            addToFixup(zero());
          }
        }

        // III)
        // calc full size;

        int lenActual = 0;
        for (List<InitNew> e : fixupBlocks) {
          lenActual += e.size();
        }

        if (lenActual > lenExpected) {
          err(".5.fixed-sized multidimensional array overflow");
        }

        // fill all with zeros?
        // size is ok. add trailing zeros.
        //
        //  short q2[4][3][2] = {  
        //      { 1 },             
        //      { 2, 3 },          
        //      { 4, 5, 6 }        
        //  };  
        if (lenExpected > lenActual) {
          for (int j = 0; j < lenExpected - lenActual; j++) {
            addToFixup(zero());
          }
        }
      }

    }

  }

  public boolean isLengthComeFromInitializersCount() {
    return lengthComeFromInitializersCount;
  }

  public void setLengthComeFromInitializersCount(boolean lengthComeFromInitializersCount) {
    this.lengthComeFromInitializersCount = lengthComeFromInitializersCount;
  }

  public CSymbol getSymbol() {
    return symbol;
  }

  public List<Integer> getDimensions() {
    return dimensions;
  }

  public int getOneElementOffset() {
    return oneElementOffset;
  }

  public List<List<InitNew>> getFixupBlocks() {
    return fixupBlocks;
  }

  public void setInitsCount(int initsCount) {
    this.initsCount = initsCount;
  }

}

public abstract class BlocksBuilder {

  private static int level = -1;
  private static List<OffsetInitializerEntry> entries = new ArrayList<OffsetInitializerEntry>(0);

  public static List<InitNew> build(CSymbol sym, Initializer initializer) {

    level = -1;
    entries = new ArrayList<OffsetInitializerEntry>(0);

    Blocks blocks = new Blocks(sym);
    buildIndices(initializer, blocks);
    blocks.merge();

    return blocks.getMergingResult();

  }

  private static void err(String m) {
    throw new ParseException(m);
  }

  private static void buildIndices(Initializer initializer, Blocks blocks) {

    if (level > blocks.getMdeep()) {
      err("too many braces.");
    }

    if (!initializer.isInitializerList()) {
      final OffsetInitializerEntry ent = new OffsetInitializerEntry(level, initializer.getAssignment());
      entries.add(ent);

      if (level == 0) {
        if (entries.size() > 1) {
          err("size.....");
        }

        blocks.pushToWild(entries);
        entries = new ArrayList<OffsetInitializerEntry>(0);
      }
    }

    else {

      ++level;
      final List<InitializerListEntry> initializers = initializer.getInitializerList();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        if (entry.isHasDesignatorsBefore()) {
          throw new ScanExc("unsupported now");
        }
        buildIndices(entry.getInitializer(), blocks);
      }
      --level;

      if (level == 0) {
        blocks.pushToBlocks(entries);
        entries = new ArrayList<OffsetInitializerEntry>(0);
      }
    }

  }
}
