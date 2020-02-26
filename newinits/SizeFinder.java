package ast.newinits;

import java.util.ArrayList;
import java.util.List;

import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast.declarations.inits.Initializer;
import ast.declarations.inits.InitializerListEntry;
import ast.errors.ParseException;
import ast.expr.util.ExprUtil;
import ast.symtabg.elements.CSymbol;

class InitBlock {

  private List<JustOut> fromBlocks;
  private List<JustOut> wildEntries;

  public InitBlock() {
    this.fromBlocks = new ArrayList<JustOut>(0);
    this.wildEntries = new ArrayList<JustOut>(0);
  }

  public void pushToBlocks(JustOut e) {
    fromBlocks.add(e);
  }

  public void pushToWild(JustOut e) {
    wildEntries.add(e);
  }

  public boolean isWildAvail(int maxlen) {
    return wildEntries.size() < maxlen;
  }

  @Override
  public String toString() {
    return "B=" + fromBlocks + "; W=" + wildEntries;
  }

  public List<JustOut> getFromBlocks() {
    return fromBlocks;
  }

  public List<JustOut> getWildEntries() {
    return wildEntries;
  }

  public void setFromBlocks(List<JustOut> fromBlocks) {
    this.fromBlocks = fromBlocks;
  }

  public void setWildEntries(List<JustOut> wildEntries) {
    this.wildEntries = wildEntries;
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
  private final List<Integer> dimensions;
  private boolean lengthComeFromInitializersCount;

  private final List<List<JustOut>> fixupBlocks;

  public Blocks(CSymbol sym) {
    this.symbol = sym;

    if (!sym.isArray()) {
      err("expect array for this initializer.");
    }
    this.dimensions = new ArrayList<Integer>(0);
    buildArrayDimensions(sym.getType(), dimensions);

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

    this.mdeep = dimensions.size();
    this.mlen = mlenTmp;
    this.blocks = new ArrayList<InitBlock>(0);
    this.fixupBlocks = new ArrayList<List<JustOut>>(0);
  }

  private void addToFixup(JustOut e) {
    boolean fixupAvail = !fixupBlocks.isEmpty() && fixupBlocks.get(fixupBlocks.size() - 1).size() < mlen;
    if (!fixupAvail) {
      fixupBlocks.add(new ArrayList<JustOut>(0));
    }
    fixupBlocks.get(fixupBlocks.size() - 1).add(e);
  }

  private void buildArrayDimensions(CType typeGiven, List<Integer> dimensions) {
    if (!typeGiven.isArray()) {
      return;
    }
    dimensions.add(typeGiven.getTpArray().getArrayLen());
    buildArrayDimensions(typeGiven.getTpArray().getArrayOf(), dimensions);
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

  public List<JustOut> getMergingResult() {
    List<JustOut> mergingResult = new ArrayList<JustOut>();
    for (List<JustOut> e : fixupBlocks) {
      mergingResult.addAll(e);
    }
    return mergingResult;
  }

  public void createEmptyWild() {
    blocks.add(new InitBlock());
  }

  public boolean isWildAvail(int maxlen) {
    return !blocks.isEmpty() && blocks.get(blocks.size() - 1).isWildAvail(maxlen);
  }

  public void pushToWild(JustOut e) {
    if (!isWildAvail(mlen)) {
      createEmptyWild();
    }
    blocks.get(blocks.size() - 1).pushToWild(e);
  }

  public void pushToBlocks(List<JustOut> entries) {
    blocks.add(new InitBlock());
    for (JustOut e : entries) {
      final InitBlock last = blocks.get(blocks.size() - 1);
      last.pushToBlocks(e);
    }
  }

  public void pushToWild(List<JustOut> entries) {
    for (JustOut e : entries) {
      pushToWild(e);
    }
  }

  private void checkBounds() {
    for (InitBlock block : blocks) {
      final List<JustOut> fromBlocks = block.getFromBlocks();
      final List<JustOut> fromWild = block.getWildEntries();

      // I)
      if (!fromBlocks.isEmpty()) {
        if (fromBlocks.size() > mlen) {
          err("extra length within nested braced-initializers.");
        }

        int maxdeepActual = 0;
        for (JustOut e : fromBlocks) {
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
      final List<JustOut> fromBlocks = block.getFromBlocks();
      final List<JustOut> fromWild = block.getWildEntries();
      // I)
      for (JustOut e : fromBlocks) {
        addToFixup(new JustOut(e.getEx()));
      }
      if (!fromBlocks.isEmpty()) {
        for (int i = fromBlocks.size(); i < mlen; i++) {
          addToFixup(zero());
        }
      }
      // II)
      for (JustOut e : fromWild) {
        addToFixup(new JustOut(e.getEx()));
      }
      if (!fromWild.isEmpty()) {
        for (int i = fromWild.size(); i < mlen; i++) {
          addToFixup(zero());
        }
      }
    }
  }

  private JustOut zero() {
    return new JustOut(ExprUtil.digitZero(symbol.getFrom()));
  }

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
      } else {
      }

    }

    else {

      if (arlen == 0) {
        if (fixupBlocks.isEmpty()) {
          err("zero-sized array with empty initializer-list not allowed");
        }
        int newlen = fixupBlocks.size();
        finishArrayType(newlen);
      } else {
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

  public List<List<JustOut>> getFixupBlocks() {
    return fixupBlocks;
  }

}

public abstract class SizeFinder {

  private static int level = -1;
  private static List<JustOut> entries = new ArrayList<JustOut>(0);

  public static void buildArraySize(CSymbol sym) {

    level = -1;
    entries = new ArrayList<JustOut>(0);

    Blocks blocks = new Blocks(sym);
    buildIndices(sym.getInitializer(), blocks);
    blocks.merge();

  }

  private static void err(String m) {
    throw new ParseException(m);
  }

  private static void buildIndices(Initializer initializer, Blocks blocks) {

    if (level > blocks.getMdeep()) {
      err("too many braces.");
    }

    if (!initializer.isInitializerList()) {
      final JustOut ent = new JustOut(initializer.getAssignment());
      ent.setLevel(level);
      entries.add(ent);

      if (level == 0) {
        if (entries.size() > 1) {
          err("size.....");
        }
        blocks.pushToWild(entries);
        entries = new ArrayList<JustOut>(0);
      }
    }

    else {

      ++level;
      final List<InitializerListEntry> initializers = initializer.getInitializerList();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        buildIndices(entry.getInitializer(), blocks);
      }
      --level;

      if (level == 0) {
        blocks.pushToBlocks(entries);
        entries = new ArrayList<JustOut>(0);
      }
    }

  }
}
