package ast._typesnew;

import jscan.symtab.Ident;

public class CEnumType {
  private final Ident tag;
  private final boolean isReference;

  public CEnumType(Ident tag, boolean isReference) {
    this.tag = tag;
    this.isReference = isReference;
  }

  public boolean isHasTag() {
    return tag != null;
  }

  public boolean isReference() {
    return isReference;
  }

  public Ident getTag() {
    return tag;
  }

}