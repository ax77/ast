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

  private static final int NESTED_INITS_LIMIT = 256; // max recursion deep
  private static final int DEFAULT_UNKNOWN_ARLEN = 65536; // int x[?][2][2]

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
      read_initializer_list(inits, type, 0, NESTED_INITS_LIMIT);
      Collections.sort(inits);
    } else {
      CExpression expr = new ParseExpression(parser).e_assign();
      inits.add(new Initializer(expr, 0));
    }

    return inits;
  }

  public List<Initializer> parse_initlist(CType type) {
    List<Initializer> inits = new ArrayList<Initializer>();
    read_initializer_list(inits, type, 0, NESTED_INITS_LIMIT);

    Collections.sort(inits);
    return inits;
  }

  private void excess(String where) {
    while (!parser.isEof()) {
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

      System.out.println("excess elements in " + where + " initializer: " + expr);
    }
  }

  private void expectOpen() {
    @SuppressWarnings("unused")
    Token jo = parser.checkedMove(T.T_LEFT_BRACE);
  }

  private void expectClose() {
    @SuppressWarnings("unused")
    Token jo = parser.checkedMove(T.T_RIGHT_BRACE);
  }

  private void addInit(List<Initializer> where, int withOffset) {
    if (parser.tok().ofType(T.T_LEFT_BRACE)) {
      parser.perror("braces around scalar initializer");
    }
    CExpression expr = new ParseExpression(parser).e_assign();
    where.add(new Initializer(expr, withOffset));
  }

  private void read_initializer_list(List<Initializer> inits, CType ty, int off, int deep) {

    // check recursion deep, to prevent stack overflow.

    if (deep <= 0) {
      parser.perror("nexted initializers too deep.");
    }

    // this condition used between array / struct
    // if only array could be nested, condition would not be necessary

    // 1)
    if (ty.isArray()) {

      boolean isHasBrace = parser.moveOptional(T.T_LEFT_BRACE);
      int arlen = (ty.getTpArray().getArrayLen() <= 0) ? DEFAULT_UNKNOWN_ARLEN : (ty.getTpArray().getArrayLen());

      CType sub = ty.getTpArray().getArrayOf();
      int elsize = sub.getSize();

      // recursion implement nested loop
      // for array: int x[3][2][2] this loop look like this:
      //
      // for (int i = 0; i < 3; i++) {
      //   for (int j = 0; j < 2; j++) {
      //     for (int k = 0; k < 2; k++) {
      //         ...
      //     }
      //   }
      // }

      int count = 0;
      for (count = 0; count < arlen; count++) {

        checkOverflow(DEFAULT_UNKNOWN_ARLEN, count);

        Token tok = parser.tok();
        if (tok.ofType(T.T_RIGHT_BRACE)) {
          break;
        }

        int offsetOf = off + elsize * count;
        boolean nestedExpansion = sub.isArray() || sub.isStrUnion();

        if (!nestedExpansion) {
          addInit(inits, offsetOf);
          parser.moveOptional(T.T_COMMA);
          continue;
        }

        // I) recursive expansion of sub-initializer
        read_initializer_list(inits, sub, offsetOf, deep - 1);
        parser.moveOptional(T.T_COMMA);

      }

      if (isHasBrace) {
        excess("array");
        expectClose();
      }

      if (ty.getTpArray().getArrayLen() <= 0) {
        ty.getTpArray().setArrayLen(count);
        ty.setSize(elsize * count);
      }

    }

    // 2)
    else if (ty.isStrUnion()) {

      expectOpen();
      int fieldIdx = 0;

      for (;;) {

        checkOverflow(DEFAULT_UNKNOWN_ARLEN, fieldIdx);

        Token tok = parser.tok();
        if (tok.ofType(T.T_RIGHT_BRACE)) {
          break;
        }

        List<CStructField> fields = ty.getTpStruct().getFields();
        if (fieldIdx == fields.size()) {
          break;
        }

        CStructField field = fields.get(fieldIdx++);
        int offsetOf = off + field.getOffset();

        CType sub = field.getType();
        boolean nestedExpansion = sub.isArray() || sub.isStrUnion();

        if (!nestedExpansion) {
          addInit(inits, offsetOf);
          parser.moveOptional(T.T_COMMA);
          continue;
        }

        // II) recursive expansion of sub-initializer
        read_initializer_list(inits, sub, offsetOf, deep - 1);
        parser.moveOptional(T.T_COMMA);

        if (!ty.isStrUnion()) {
          System.out.println("parse initializer: not a struct: " + ty.toString());
          break;
        }

      }

      excess("struct");
      expectClose();
    }

    // 3)
    else {
      System.out.println("III");
      CType arraytype = new CType(new CArrayType(ty, 1));
      read_initializer_list(inits, arraytype, off, deep - 1);
    }
  }

  private void checkOverflow(int guard, int initsCnt) {
    parser.unexpectedEof();
    if (initsCnt >= guard) {
      parser.perror("struct/array initializer list too big...");
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
