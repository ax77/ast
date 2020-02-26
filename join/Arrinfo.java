package ast.join;

import java.util.ArrayList;
import java.util.List;

import ast._typesnew.CType;
import ast.errors.ParseException;
import ast.symtabg.elements.CSymbol;

public class Arrinfo {
  private final CType type;
  private List<Integer> elemsPerBlockAtLevel;
  private int oneElementOffset;
  private boolean isZeroFirstDimension;
  private boolean isMultiDimensionalArray;
  private List<Integer> arrayDimensions;
  private List<Integer> indexedOffsets; // for designator offset calculation

  public Arrinfo(CType type) {
    this.type = type;
    if (!type.isArray()) {
      throw new ParseException("expect array, but was: " + type.toString());
    }

    this.elemsPerBlockAtLevel = new ArrayList<Integer>(0);
    this.oneElementOffset = -1;

    buildInfo();
    buildIndexOffsets();
  }

  private void getArrayInfo(CType type, List<Integer> dims) {
    if (!type.isArray()) {
      this.oneElementOffset = type.getSize();
      return;
    }
    dims.add(type.getTpArray().getArrayLen());
    getArrayInfo(type.getTpArray().getArrayOf(), dims);
  }

  private void buildInfo() {

    List<Integer> dims = new ArrayList<Integer>();
    getArrayInfo(type, dims);

    if (dims.isEmpty()) {
      throw new ParseException("internal error: no dimension for array.");
    }

    for (int j = 1; j < dims.size(); j++) {
      if (dims.get(j) <= 0) {
        throw new ParseException("unspecified array size.");
      }
    }

    this.isZeroFirstDimension = dims.get(0) <= 0;
    this.isMultiDimensionalArray = dims.size() > 1;
    this.arrayDimensions = new ArrayList<Integer>(dims);

    while (!dims.isEmpty()) {
      int x = dims.remove(0);
      if (x <= 0) {
        elemsPerBlockAtLevel.add(-1);
      } else {
        for (Integer i : dims) {
          x *= i.intValue();
        }
        elemsPerBlockAtLevel.add(x);
      }
    }

  }

  public void buildIndexOffsets() {

    indexedOffsets = new ArrayList<Integer>();

    List<Integer> dimscopy = new ArrayList<Integer>(arrayDimensions);
    dimscopy.add(oneElementOffset);

    dimscopy.remove(0);

    while (!dimscopy.isEmpty()) {
      int x = dimscopy.remove(0);
      for (Integer i : dimscopy) {
        x *= i.intValue();
      }
      indexedOffsets.add(x);
    }

    // XXX: note about offset calculation
    //
    //    // have:
    //    // arr[2][3][4]
    //    //
    //    // need:
    //    // (3*4*sizeof(int)) | (4*sizeof(int)) | (sizeof(int))
    //
    //    List<Integer> list = new ArrayList<Integer>();
    //    list.add(3); // second dim
    //    list.add(4); // third dim
    //    list.add(4); // elem size
    //
    //    List<Integer> result = new ArrayList<Integer>();
    //    while (!list.isEmpty()) {
    //      int x = list.remove(0);
    //      for (Integer i : list) {
    //        x *= i.intValue();
    //      }
    //      result.add(x);
    //    }
    //    // x[1][2][3] = (1*(3*4*4)) + (2*(4*4)) + (3*(4)) = 92 :: offset of base
    //    // [48, 16, 4]
    //    System.out.println(result);
  }

  public List<Integer> getIndexedOffsets() {
    return indexedOffsets;
  }

  public List<Integer> getElemsPerBlockAtLevel() {
    return elemsPerBlockAtLevel;
  }

  public int getOneElementOffset() {
    return oneElementOffset;
  }

  public boolean isZeroFirstDimension() {
    return isZeroFirstDimension;
  }

  public boolean isMultiDimensionalArray() {
    return isMultiDimensionalArray;
  }

  public List<Integer> getArrayDimensions() {
    // XXX: destructive operations with this list. always return a copy!
    return new ArrayList<Integer>(arrayDimensions);
  }

}
