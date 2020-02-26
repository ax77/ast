package ast.declarations.parser;

import java.util.ArrayList;
import java.util.List;

import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.main.StorageKind;
import ast._typesnew.parser.ParseBase;
import ast._typesnew.parser.ParseDecl;
import ast._typesnew.util.TypeMerger;
import ast.declarations.inits.Designator;
import ast.declarations.inits.Initializer;
import ast.declarations.inits.InitializerListEntry;
import ast.declarations.main.Declaration;
import ast.declarations.sem.FinalizeInitializers;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.CSymbolBase;
import jscan.hashed.Hash_ident;
import jscan.sourceloc.SourceLocation;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseDeclarations {
  private final Parse parser;
  private CType basetype;
  private StorageKind storagespec;

  public ParseDeclarations(Parse parser) {
    this.parser = parser;
  }

  // constructor need for entry point.
  // when we parse basetype, and don't know yet, function is, or declaration
  // and build first
  public ParseDeclarations(Parse parser, CType basetype, StorageKind storagespec) {
    this.parser = parser;
    this.basetype = basetype;
    this.storagespec = storagespec;
  }

  //declaration
  //    : declaration_specifiers ';'
  //    | declaration_specifiers init_declarator_list ';'
  //    ;

  //  init_declarator_list
  //    : init_declarator
  //    | init_declarator_list ',' init_declarator
  //    ;

  //  init_declarator
  //    : declarator '=' initializer
  //    | declarator
  //    ;

  //  initializer
  //    : assignment_expression
  //    | '{' initializer_list '}'
  //    | '{' initializer_list ',' '}'
  //    ;

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

  //  designation
  //    : designator_list '='
  //    ;
  //
  //  designator_list
  //      : designator
  //      | designator_list designator
  //      ;
  //  
  //  designator
  //      : '[' constant_expression ']'
  //      | '.' IDENTIFIER
  //      ;

  //  declaration
  //      : declaration_specifiers ';'
  //      | declaration_specifiers init_declarator_list ';'
  //      | static_assert_declaration
  //      ;

  public Declaration parseDeclaration() {

    Token startLocation = parser.tok();

    // TODO: more clean.
    if (isStaticAssertAndItsOk()) {
      return new Declaration();
    }

    ParseBase pb = new ParseBase(parser);
    basetype = pb.parseBase();
    storagespec = pb.getStorageSpec();
    NullChecker.check(basetype, storagespec); // paranoia

    /// this may be struct/union/enum declaration
    ///
    if (parser.tp() == T.T_SEMI_COLON) {
      Token endLocation = parser.semicolon();

      boolean isStructUnionEnum = basetype.isStrUnion() || basetype.isEnumeration();
      if (!isStructUnionEnum) {
        parser.perror("expect struct/union/enum declaration. but was: " + basetype.toString());
      }

      // semicolon after mean: this declaration has no name, no declarator after...
      // if this aggregate declared without name in function-scope, it NOT change stack-size.

      final Declaration agregate = new Declaration(startLocation, endLocation, basetype);
      return agregate;
    }

    List<CSymbol> initDeclaratorList = parseInitDeclaratorList();
    Token endLocation = parser.semicolon();

    final Declaration declaration = FinalizeInitializers.sVarlist(startLocation, endLocation, initDeclaratorList);
    return declaration;
  }

  //  static_assert_declaration
  //    : STATIC_ASSERT '(' constant_expression ',' STRING_LITERAL ')' ';'
  //    ;

  public boolean isStaticAssertAndItsOk() {
    if (!parser.tok().isIdent(Hash_ident._Static_assert_ident)) {
      return false;
    }

    parser.checkedMoveIdent(Hash_ident._Static_assert_ident);
    parser.lparen();

    CExpression ce = new ParseExpression(parser).e_const_expr();
    parser.checkedMove(T.T_COMMA);

    Token message = parser.checkedGetT(T.TOKEN_STRING);
    parser.rparen();
    parser.semicolon();

    long sares = new ConstexprEval(parser).ce(ce);
    if (sares == 0) {
      parser.perror("static-assert fail with message: " + message.getValue());
    }

    return true;
  }

  public List<CSymbol> parseInitDeclaratorList() {
    List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

    CSymbol initDeclarator = parseInitDeclarator();
    initDeclaratorList.add(initDeclarator);

    while (parser.tp() == T.T_COMMA) {
      parser.move();

      CSymbol initDeclaratorSeq = parseInitDeclarator();
      initDeclaratorList.add(initDeclaratorSeq);
    }

    return initDeclaratorList;
  }

  public CSymbol parseInitDeclarator() {
    //  init_declarator
    //    : declarator '=' initializer
    //    | declarator
    //    ;

    Token saved = parser.tok();

    CDecl decl = new ParseDecl(parser).parseDecl();
    CType type = TypeMerger.build(basetype, decl);

    if (parser.tp() != T.T_ASSIGN) {
      CSymbolBase base = CSymbolBase.SYM_LVAR;
      if (storagespec == StorageKind.ST_TYPEDEF) {
        base = CSymbolBase.SYM_TYPEDEF;
      }
      final CSymbol sym = new CSymbol(base, decl.getName(), type, saved);
      parser.defineSym(decl.getName(), sym);
      return sym;
    }

    parser.checkedMove(T.T_ASSIGN);
    Initializer initializer = parseInitializer();

    if (storagespec == StorageKind.ST_TYPEDEF) {
      parser.perror("typedef with initializer.");
    }

    final CSymbol sym = new CSymbol(CSymbolBase.SYM_LVAR, decl.getName(), type, initializer, saved);
    parser.defineSym(decl.getName(), sym);

    return sym;
  }

  public Initializer parseInitializer() {

    //  initializer
    //    : assignment_expression
    //    | '{' initializer_list '}'
    //    | '{' initializer_list ',' '}'
    //    ;
    //
    //  initializer_list
    //    : initializer
    //    | initializer_list ',' initializer
    //    ;

    SourceLocation location = new SourceLocation(parser.tok());

    if (parser.tp() != T.T_LEFT_BRACE) {

      CExpression assignment = new ParseExpression(parser).e_assign();
      return new Initializer(assignment, location);
    }

    parser.checkedMove(T.T_LEFT_BRACE);

    // if is empty array initialization - return initializer with empty initializer-list
    // int a[5] = {};
    if (parser.tp() == T.T_RIGHT_BRACE) {
      parser.checkedMove(T.T_RIGHT_BRACE);
      return new Initializer(new ArrayList<InitializerListEntry>(0), location);
    }

    // otherwise - recursively expand braced initializers
    //
    List<InitializerListEntry> initializerList = parseInitializerList(); // XXX: taint comma case here
    parser.checkedMove(T.T_RIGHT_BRACE);

    return new Initializer(initializerList, location);
  }

  public List<InitializerListEntry> parseInitializerList() {

    List<InitializerListEntry> initializerList = new ArrayList<InitializerListEntry>(0);

    // c89
    //  initializer_list
    //    : initializer
    //    | initializer_list ',' initializer
    //    ;

    // c99
    //  initializer_list
    //    : designation initializer
    //    | initializer
    //    | initializer_list ',' designation initializer
    //    | initializer_list ',' initializer
    //    ;

    InitializerListEntry entry = parseInitializerListEntry();
    initializerList.add(entry);

    while (parser.tp() == T.T_COMMA) {

      // | '{' initializer_list ',' '}'
      //
      Token lookBrace = parser.getTokenlist().peek();
      if (lookBrace.ofType(T.T_RIGHT_BRACE)) {
        parser.checkedMove(T.T_COMMA);
        return initializerList;
      }

      parser.checkedMove(T.T_COMMA);

      InitializerListEntry initializerSeq = parseInitializerListEntry();
      initializerList.add(initializerSeq);
    }

    return initializerList;
  }

  private InitializerListEntry parseInitializerListEntry() {

    // initializer_list
    //   : designation initializer
    //   | initializer
    //   | initializer_list ',' designation initializer
    //   | initializer_list ',' initializer
    //   ;
    // 
    // designation
    //   : designator_list '='
    //   ;
    // 
    // designator_list
    //   : designator
    //   | designator_list designator
    //   ;
    // 
    // designator
    //   : '[' constant_expression ']'
    //   | '.' IDENTIFIER
    //   ;

    SourceLocation location = new SourceLocation(parser.tok());

    if (parser.tp() == T.T_LEFT_BRACKET || parser.tp() == T.T_DOT) {
      List<Designator> designators = parseDesignatorList(parser);
      parser.checkedMove(T.T_ASSIGN);

      Initializer initializer = parseInitializer();
      return new InitializerListEntry(designators, initializer, location);
    }

    Initializer initializer = parseInitializer();
    return new InitializerListEntry(initializer, location);
  }

  private List<Designator> parseDesignatorList(Parse p) {

    List<Designator> designators = new ArrayList<Designator>(0);

    // designator_list
    //   : designator
    //   | designator_list designator
    //   ;
    // 
    // designator
    //   : '[' constant_expression ']'
    //   | '.' IDENTIFIER
    //   ;

    for (;;) {

      if (p.tp() == T.T_LEFT_BRACKET) {
        p.lbracket();
        CExpression expr = new ParseExpression(p).e_const_expr();
        p.rbracket();
        designators.add(new Designator(expr));
      }

      else if (p.tp() == T.T_DOT) {
        p.move();
        Token ident = p.expectIdentifier();
        designators.add(new Designator(ident));
      }

      else {
        break;
      }
    }

    return designators;

  }

}
