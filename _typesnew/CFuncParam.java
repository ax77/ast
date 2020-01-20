package ast._typesnew;

import jscan.symtab.Ident;
import ast.parse.NullChecker;

public class CFuncParam {
  private final Ident name;
  private CType type; // we apply the type, when build old-style function identifier-list+declarations

  public CFuncParam(Ident name, CType type) {
    NullChecker.check(name, type);
    this.name = name;
    this.type = type;
  }

  // KnR identifier-list func-definition [int x(a,b,c) int a,b,c; {}]
  public CFuncParam(Ident name) {
    NullChecker.check(name);
    this.name = name;
    this.type = null;
  }

  // abstract func-declaration [int x(char*, int*);]
  public CFuncParam(CType type) {
    NullChecker.check(type);
    this.name = null;
    this.type = type;
  }

  public Ident getName() {
    return name;
  }

  public CType getType() {
    return type;
  }

  public boolean isHasName() {
    return name != null;
  }

  public boolean isHasType() {
    return type != null;
  }

  public void setType(CType type) {
    this.type = type;
  }

}