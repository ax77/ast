package ast._typesnew.sem;

import java.util.List;

import ast._typesnew.CStructField;
import ast._typesnew.CStructType;
import ast.parse.Parse;

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
