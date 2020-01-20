package ast._typesnew;

import java.util.List;

import ast.parse.ParseException;
import jscan.symtab.Ident;

public class CStructType {
  private final boolean isUnion;
  private final Ident tag;
  private final List<CStructField> fields; // list, because we need original declaration's order without sorting
  private final boolean isReference;
  private boolean isIncomplete; // TODO:

  public CStructType(boolean isUnion, Ident tag) {
    this.isUnion = isUnion;
    this.tag = tag;
    this.fields = null;
    this.isReference = true;
  }

  public CStructType(boolean isUnion, Ident tag, List<CStructField> fields) {
    this.isUnion = isUnion;
    this.tag = tag;
    this.fields = fields;
    this.isReference = false;
  }

  public boolean isHasConstFields() {
    if (isReference || isIncomplete) {
      throw new ParseException("TODO: you want get fields from incomplete...");
    }
    for (CStructField f : fields) {
      final CType type = f.getType();
      if (type.isConst()) {
        return true;
      }
    }
    return false;
  }

  public boolean isUnion() {
    return isUnion;
  }

  public Ident getTag() {
    return tag;
  }

  public List<CStructField> getFields() {
    checkHasFields();
    return fields;
  }

  private void checkHasFields() {
    if (isReference) {
      throw new ParseException("internal error: struct reference has no fields.");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    final String str = tag == null ? "<no-tag>" : "tag=" + tag.getName() + " ";
    sb.append((isUnion ? "UNION " : "STRUCT ") + str);
    if (!isReference) {
      sb.append(fields.toString());
    }
    return sb.toString();
  }

  public boolean isHasTag() {
    return tag != null;
  }

  public boolean isReference() {
    return isReference;
  }

  public boolean isHasField(String s) {
    checkHasFields();
    for (CStructField f : fields) {
      if (!f.isHasName()) {
        continue;
      }
      if (f.getName().getName().equals(s)) {
        return true;
      }
    }
    return false;
  }

  public boolean isIncomplete() {
    return isIncomplete;
  }

}