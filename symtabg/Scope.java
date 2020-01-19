package ast.symtabg;

import java.util.HashMap;
import java.util.Map.Entry;

/// Represent one scope
/// file or block
public class Scope<K, V> {
  private final String scopeName;
  private final HashMap<K, V> scope;

  public Scope(String scopeName) {
    this.scopeName = scopeName;
    this.scope = new HashMap<K, V>();
  }

  public void put(K key, V value) {
    scope.put(key, value);
  }

  public V get(K key) {
    return scope.get(key);
  }

  public String getScopeName() {
    return scopeName;
  }

  public HashMap<K, V> getScope() {
    return scope;
  }

  public boolean isEmpty() {
    return scope.isEmpty();
  }

  public void dump() {
    System.out.println("scope-name: " + scopeName);
    for (Entry<K, V> e : scope.entrySet()) {
      System.out.println(e.getKey().toString() + " " + e.getValue().toString());
      System.out.println();
    }

  }

}
