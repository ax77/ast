package ast._typesnew;

import java.util.HashMap;
import java.util.Map;

import jscan.symtab.Ident;

public class CEnumType {
  private final Ident tag;
  private final boolean isReference;
  private final Map<Ident, Integer> enumerators; // need only for type-compatible routine.

  public CEnumType(Ident tag) {
    this.tag = tag;
    this.isReference = true;
    this.enumerators = new HashMap<Ident, Integer>();
  }

  public CEnumType(Ident tag, Map<Ident, Integer> enumerators) {
    this.tag = tag;
    this.isReference = false;
    this.enumerators = enumerators;
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

  public Map<Ident, Integer> getEnumerators() {
    return enumerators;
  }

}