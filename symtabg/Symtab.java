package ast.symtabg;

import java.util.ArrayList;
import java.util.List;

import ast.parse.ParseException;

public class Symtab<K, V> {

  // this is simple stack
  // when create new scope, push it on top
  // when add new symbol, get top table, and put symbol on it

  private List<Scope<K, V>> scopes;
  private List<Integer> scopeNumerator;

  public Symtab() {
    this.scopes = new ArrayList<Scope<K, V>>();

    this.scopeNumerator = new ArrayList<Integer>(0);

    this.scopeNumerator.add(scopeNumerator.size());
  }

  private String getNum() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < scopeNumerator.size(); i++) {
      sb.append(String.format("%d", i));
      if (i < scopeNumerator.size() - 1) {
        sb.append(".");
      }
    }
    return sb.toString();
  }

  public void pushscope(String currentFunctionName) {
    String scopeName = currentFunctionName + "." + getNum();
    this.scopes.add(0, new Scope<K, V>(scopeName));

    this.scopeNumerator.add(scopeNumerator.size());
  }

  public void popscope() {
    if (scopes.isEmpty()) {
      throw new ParseException("empty symbol table. no scopes are available");
    }
    this.scopes.remove(0);

    this.scopeNumerator.remove(scopeNumerator.size() - 1);
  }

  public boolean isFileScope() {
    return scopes.size() == 1;
  }

  public boolean isBlockScope() {
    return scopes.size() > 1;
  }

  public V getsym(K name) {
    for (int i = 0; i < scopes.size(); i++) {
      Scope<K, V> scope = scopes.get(i);
      V sym = scope.get(name);
      if (sym != null) {
        return sym;
      }
    }
    return null;
  }

  // this need __ONLY__ when we define new symbol, and check redefinition
  // others symbols we get from bottom to top scope to scope...
  public V getsymFromCurrentScope(K name) {
    Scope<K, V> scope = scopes.get(0);
    V sym = scope.get(name);
    if (sym != null) {
      return sym;
    }
    return null;
  }

  public void addsym(K key, V sym) {
    if (scopes.isEmpty()) {
      throw new ParseException("empty symbol table. no scopes are available");
    }
    if (key == null) {
      throw new ParseException("null key for symbol entry...");
    }
    Scope<K, V> scope = scopes.get(0);
    scope.put(key, sym);
  }

  public boolean isEmpty() {
    return scopes.isEmpty();
  }

  public void dump() {
    for (int i = 0; i < scopes.size(); i++) {
      Scope<K, V> scope = scopes.get(i);
      scope.dump();
    }
  }
}
