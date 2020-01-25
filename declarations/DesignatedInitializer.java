package ast.declarations;

import ast.errors.ParseException;

public class DesignatedInitializer {

  //  initializer_list
  //    : designation initializer
  //    | initializer
  //    | initializer_list ',' designation initializer
  //    | initializer_list ',' initializer
  //    ;

  private final Designation designation;
  private final Initializer initializer;

  public DesignatedInitializer(Designation designation, Initializer initializer) {

    //TODO:POSITION
    if (designation == null || initializer == null) {
      throw new ParseException("designation error... TODO...");
    }

    this.designation = designation;
    this.initializer = initializer;
  }

  public Designation getDesignation() {
    return designation;
  }

  public Initializer getInitializer() {
    return initializer;
  }

}
