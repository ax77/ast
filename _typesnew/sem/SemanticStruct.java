package ast._typesnew.sem;

import java.util.List;

import ast._typesnew.CStructField;
import ast._typesnew.CStructType;
import ast.parse.Parse;

public class SemanticStruct {

  private final Parse p;

  public SemanticStruct(Parse p) {
    this.p = p;
  }

  public CStructUnionSizeAlign finalizeStructType(CStructType tpStruct) {
    List<CStructField> fields = null;
    if (!tpStruct.isReference()) {
      fields = tpStruct.getFields();
    }
    return new CStructUnionSizeAlign(tpStruct.isUnion(), tpStruct.isReference(), fields);
  }

}
