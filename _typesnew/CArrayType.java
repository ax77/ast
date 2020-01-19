package ast._typesnew;

import ast.parse.NullChecker;
import ast.parse.ParseException;

public class CArrayType {
  private final CType arrayOf;
  private int arrayLen;
  private boolean isIncomplete; // TODO:

  public CArrayType(CType arrayof, int arrayLen) {
    NullChecker.check(arrayof); // always must be present. but init-expression - optional, check it later. int x[][2];

    if (arrayLen <= 0) {
      //throw new ScanExc("zero or negative sized array. TODO:");
    }

    if (arrayof.isBitfield()) {
      throw new ParseException("error: array of bitfield.");
    }

    this.arrayOf = arrayof;
    this.arrayLen = arrayLen;
  }

  public CType getArrayOf() {
    return arrayOf;
  }

  @Override
  public String toString() {
    return "array_of(" + String.format("%d", arrayLen) + " " + arrayOf.toString() + ")";
  }

  public int getArrayLen() {
    return arrayLen;
  }

  public void setArrayLen(int arrayLen) {
    this.arrayLen = arrayLen;
  }

  public boolean isIncomplete() {
    return isIncomplete;
  }

}