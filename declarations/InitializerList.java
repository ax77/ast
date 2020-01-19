package ast.declarations;

import java.util.ArrayList;
import java.util.List;

import ast.parse.ParseException;

public class InitializerList {

  //XXX: c89
  //  initializer_list
  //    : initializer
  //    | initializer_list ',' initializer
  //    ;

  //XXX: c99
  //  initializer_list
  //    : designation initializer
  //    | initializer
  //    | initializer_list ',' designation initializer
  //    | initializer_list ',' initializer
  //    ;

  private final List<InitializerListEntry> initializers;

  public InitializerList() {
    this.initializers = new ArrayList<InitializerListEntry>(0);
  }

  public void push(InitializerListEntry entry) {
    if (entry == null) {
      //TODO:POSITION
      throw new ParseException("null entry...");
    }
    initializers.add(entry);
  }

  public List<InitializerListEntry> getInitializers() {
    return initializers;
  }

}
