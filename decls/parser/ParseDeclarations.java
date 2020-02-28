package ast.decls.parser;

import java.util.ArrayList;
import java.util.Collections;
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
import ast.types.CArrayType;
import ast.types.CStructField;
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
    List<Initializer> inits = new ArrayList<Initializer>();

    if (parser.tok().ofType(T.T_LEFT_BRACE)) {
      read_initializer_list(inits, type, 0);
      Collections.sort(inits);
    } else {
      CExpression expr = new ParseExpression(parser).e_assign();
      inits.add(new Initializer(expr, 0));
    }

    return inits;
  }

  public List<Initializer> parse_initlist(CType type) {
    List<Initializer> inits = new ArrayList<Initializer>();
    read_initializer_list(inits, type, 0);

    Collections.sort(inits);
    return inits;
  }

  private void warningExcess() {
    for (;;) {
      Token tok = parser.tok();
      if (tok.ofType(T.T_RIGHT_BRACE)) {
        return;
      }
      if (tok.ofType(T.TOKEN_EOF)) {
        parser.perror("unexpected EOF in initializer-list");
      }
      if (tok.ofType(T.T_DOT)) {
        parser.perror("unimpl. skip designations.");
      }
      CExpression expr = new ParseExpression(parser).e_assign();
      parser.moveOptional(T.T_COMMA);

      System.out.println("excess elements in initizlizer: " + expr);
    }
  }

  private void read_initializer_list(List<Initializer> inits, CType ty, int off) {

    // 1)
    if (ty.isArray()) {

      boolean has_brace = parser.moveOptional(T.T_LEFT_BRACE);

      boolean flexible = (ty.getTpArray().getArrayLen() <= 0);
      int elemsize = ty.getTpArray().getArrayOf().getSize();

      final CType subtype = ty.getTpArray().getArrayOf();
      int i = 0;

      for (i = 0; flexible || i < ty.getTpArray().getArrayLen(); i++) {

        Token tok = parser.tok();
        if (tok.ofType(T.T_RIGHT_BRACE)) {
          break;
        }

        int nextoffset = off + elemsize * i;

        if (subtype.isArray() || subtype.isStrUnion()) {
          read_initializer_list(inits, subtype, nextoffset);
        } else {
          CExpression expr = new ParseExpression(parser).e_assign();
          inits.add(new Initializer(expr, nextoffset));
        }

        parser.moveOptional(T.T_COMMA);

      }

      if (has_brace) {
        warningExcess();
        parser.checkedMove(T.T_RIGHT_BRACE);
      }

      if (ty.getTpArray().getArrayLen() <= 0) {
        ty.getTpArray().setArrayLen(i);
        ty.setSize(elemsize * i);
      }

    }

    // 2)
    else if (ty.isStrUnion()) {

      boolean has_brace = parser.moveOptional(T.T_LEFT_BRACE);
      int i = 0;

      for (;;) {
        Token tok = parser.tok();

        if (tok.ofType(T.T_RIGHT_BRACE)) {
          parser.checkedMove(T.T_RIGHT_BRACE);
          return;
        }

        if (i == ty.getTpStruct().getFields().size()) {
          break;
        }

        CStructField field = ty.getTpStruct().getFields().get(i++);
        CType subtype = field.getType();

        int nextoffset = off + field.getOffset();

        if (subtype.isArray() || subtype.isStrUnion()) {
          read_initializer_list(inits, subtype, nextoffset);
        } else {
          CExpression expr = new ParseExpression(parser).e_assign();
          inits.add(new Initializer(expr, nextoffset));
        }

        parser.moveOptional(T.T_COMMA);

        if (!ty.isStrUnion()) {
          break;
        }

      }

      if (has_brace) {
        parser.checkedMove(T.T_RIGHT_BRACE);
      }
    }

    // 3)
    else {
      CType arraytype = new CType(new CArrayType(ty, 1));
      read_initializer_list(inits, arraytype, off);
    }
  }

  //  static_assert_declaration
  //    : STATIC_ASSERT '(' constant_expression ',' STRING_LITERAL ')' ';'
  //    ;

  public boolean isStaticAssertAndItsOk() {
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
