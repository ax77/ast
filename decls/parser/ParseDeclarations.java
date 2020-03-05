package ast.decls.parser;

import java.util.ArrayList;
import java.util.List;

import jscan.hashed.Hash_ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.decls.Declaration;
import ast.decls.Initializer;
import ast.expr.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.NullChecker;
import ast.parse.Parse;
import ast.symtab.elements.CSymbol;
import ast.symtab.elements.CSymbolBase;
import ast.types.CType;
import ast.types.decl.CDecl;
import ast.types.main.StorageKind;
import ast.types.parser.ParseBase;
import ast.types.parser.ParseDecl;
import ast.types.util.TypeMerger;

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

  public Declaration parse() {

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

    final Declaration declaration = new Declaration(startLocation, endLocation, initDeclaratorList);
    return declaration;
  }

  private List<CSymbol> parseInitDeclaratorList() {
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

      CSymbol sym1 = new CSymbol(base, decl.getName(), type, saved);
      parser.defineSym(decl.getName(), sym1);

      return sym1;
    }

    parser.checkedMove(T.T_ASSIGN);
    List<Initializer> inits = parseInitializer(type);

    if (storagespec == StorageKind.ST_TYPEDEF) {
      parser.perror("typedef with initializer.");
    }

    CSymbol sym = new CSymbol(CSymbolBase.SYM_LVAR, decl.getName(), type, inits, saved);
    parser.defineSym(decl.getName(), sym);

    return sym;
  }

  public List<Initializer> parseInitializer(CType type) {

    // nested list

    if (parser.tok().ofType(T.T_LEFT_BRACE)) {
      return new ParseInitializerList(parser, type).parse();
    }

    // just expression

    List<Initializer> inits = new ArrayList<Initializer>();
    CExpression expr = new ParseExpression(parser).e_assign();

    inits.add(new Initializer(expr, 0));
    return inits;

  }

  public List<Initializer> parseInitializerList(CType type) {
    return new ParseInitializerList(parser, type).parse();
  }

  public boolean isStaticAssertAndItsOk() {

    //  static_assert_declaration
    //    : STATIC_ASSERT '(' constant_expression ',' STRING_LITERAL ')' ';'
    //    ;

    if (!parser.tok().isIdent(Hash_ident._Static_assert_ident)) {
      return false;
    }

    parser.checkedMove(Hash_ident._Static_assert_ident);
    parser.lparen();

    CExpression ce = new ParseExpression(parser).e_const_expr();
    parser.checkedMove(T.T_COMMA);

    Token message = parser.checkedMove(T.TOKEN_STRING);
    parser.rparen();
    parser.semicolon();

    long sares = new ConstexprEval(parser).ce(ce);
    if (sares == 0) {
      parser.perror("static-assert fail with message: " + message.getValue());
    }

    return true;
  }

}
