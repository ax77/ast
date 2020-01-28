package ast.arrayinit;

import java.util.ArrayList;
import java.util.List;

import ast._typesnew.CType;
import ast.errors.ParseException;
import ast.parse.NullChecker;

public class ArrayDimensionsExpander {

  // TODO: location

  private final CType type;
  private final List<Integer> dims;
  private final List<Integer> esiz;
  private final int fullArrayLen;

  public List<Integer> getEsiz() {
    return esiz;
  }

  public ArrayDimensionsExpander(CType type) {
    NullChecker.check(type);

    if (!type.isArray()) {
      throw new ParseException("internal error: " + "expected array for dimension expander");
    }

    this.type = type;
    this.dims = new ArrayList<Integer>(0);
    this.esiz = new ArrayList<Integer>(0);

    buildArrayDimensions(this.type, this.dims, this.esiz);

    if (dims.size() != esiz.size()) {
      throw new ParseException("internal error: " + "size diff.");
    }

    if (dims.isEmpty()) {
      throw new ParseException("internal error: " + "empty array dimensions");
    }

    if (dims.get(0).intValue() == 0) {
      throw new ParseException("internal error: " + "computed array len unimplimented");
    }

    // full-len
    int len = 1;
    for (Integer i : dims) {
      len *= i.intValue();
    }
    this.fullArrayLen = len;
  }

  private void buildArrayDimensions(CType typeGiven, List<Integer> dimsOut, List<Integer> esizOut) {
    if (!typeGiven.isArray()) {
      return;
    }
    dimsOut.add(typeGiven.getTpArray().getArrayLen());
    esizOut.add(typeGiven.getSize() / typeGiven.getTpArray().getArrayLen());
    buildArrayDimensions(typeGiven.getTpArray().getArrayOf(), dimsOut, esizOut);
  }

  public List<Integer> getDims() {
    return dims;
  }

  public int getFullArrayLen() {
    return fullArrayLen;
  }

}