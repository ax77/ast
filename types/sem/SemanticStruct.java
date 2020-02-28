package ast.types.sem;

import java.util.List;

import ast.parse.Parse;
import ast.types.CStructField;
import ast.types.CStructType;

public class SemanticStruct {

  private final Parse parser;

  public SemanticStruct(Parse p) {
    this.parser = p;
  }

  public StructAligner finalizeStructType(CStructType tpStruct) {
    List<CStructField> fields = null;
    if (!tpStruct.isIncomplete()) {
      fields = tpStruct.getFields();
    }
    return new StructAligner(tpStruct.isUnion(), tpStruct.isIncomplete(), fields);
  }

}
