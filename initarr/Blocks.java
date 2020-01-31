package ast.initarr;

import java.util.ArrayList;
import java.util.List;

import jscan.tokenize.Token;
import ast._typesnew.CArrayType;
import ast._typesnew.CType;
import ast.errors.ParseException;
import ast.expr.sem.CExpressionBuilderHelper;
import ast.symtabg.elements.CSymbol;

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

public class Blocks {

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

  private List<InitNew> mergingResult;
  private int initsCount;
  private final List<Integer> dimensions;

  private final int oneElementOffset; // XXX: for int arr[][2][3] is INT_SIZE
  private boolean lengthComeFromInitializersCount;

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
    this.mergingResult = new ArrayList<InitNew>(0);
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
    finishArrayType();
    fixOffsets();
  }

  private void buildResultList() {
    Token from = symbol.getFrom();

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
        mergingResult.add(new InitNew(e.getExpression()));
      }
      if (!fromBlocks.isEmpty()) {
        for (int i = fromBlocks.size(); i < mlen; i++) {
          mergingResult.add(new InitNew(CExpressionBuilderHelper.digitZero(from)));
        }
      }

      // II)
      for (OffsetInitializerEntry e : fromWild) {
        mergingResult.add(new InitNew(e.getExpression()));
      }
      if (!fromWild.isEmpty()) {
        for (int i = fromWild.size(); i < mlen; i++) {
          mergingResult.add(new InitNew(CExpressionBuilderHelper.digitZero(from)));
        }
      }

    }

  }

  private void finishArrayType() {
    Token from = symbol.getFrom();

    // int x[1] = { 1,2,3 };
    // int x[] = { ... };
    if (dimensions.get(0) != 0) {
      if (initsCount > dimensions.get(0)) {
        err("array size overflow.");
      } else {
        setArrayLen();
      }
    }

    else {
      dimensions.set(0, initsCount);
      setArrayLen();
    }

    // int x[2][3][4] = { { {  } } };
    // fill full array with zero's
    //
    if (initsCount == 0) {
      int fulllen = getFullLen();

      for (int i = 0; i < fulllen; i++) {
        mergingResult.add(new InitNew(CExpressionBuilderHelper.digitZero(from)));
      }
    }

    // trailing 
    final int needSize = getFullLen();
    final int haveSize = mergingResult.size();
    if (haveSize < needSize) {
      for (int j = haveSize; j < needSize; j++) {
        mergingResult.add(new InitNew(CExpressionBuilderHelper.digitZero(from)));
      }
    }

  }

  private void setArrayLen() {
    CType type = symbol.getType();
    CArrayType arr = type.getTpArray();

    if (arr.getArrayLen() != 0) {
      return;
    }

    arr.setArrayLen(initsCount);

    type.setSize(arr.getArrayLen() * arr.getArrayOf().getSize());
    type.setAlign(arr.getArrayOf().getAlign());
  }

  private void fixOffsets() {
    int offset = 0;
    for (InitNew e : mergingResult) {
      e.setOffset(offset);
      offset += oneElementOffset;
    }
  }

  private int getFullLen() {
    checkNoZeroDimensions();

    int fulllen = 1;
    for (Integer i : dimensions) {
      fulllen *= i.intValue();
    }

    return fulllen;
  }

  private void checkNoZeroDimensions() {
    for (Integer i : dimensions) {
      if (i == 0) {
        err("zero len.");
      }
    }
  }

}
