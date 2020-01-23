package ast._typesnew;

public class CIncompleteType {
  private CStructType tpStructUnion;
  private CArrayType tpArray;

  private boolean isIncompleteStructUnion;
  private boolean isIncompleteArray;

  public CIncompleteType(CStructType tpStruct) {
    this.tpStructUnion = tpStruct;
    this.isIncompleteStructUnion = true;
  }

  public CIncompleteType(CArrayType tpArray) {
    this.tpArray = tpArray;
    this.isIncompleteArray = true;
  }

  public CStructType getTpStruct() {
    return tpStructUnion;
  }

  public CArrayType getTpArray() {
    return tpArray;
  }

  public boolean isIncompleteStruct() {
    return isIncompleteStructUnion && !tpStructUnion.isUnion();
  }

  public boolean isIncompleteUnion() {
    return isIncompleteStructUnion && tpStructUnion.isUnion();
  }

  public boolean isIncompleteArray() {
    return isIncompleteArray;
  }

}
