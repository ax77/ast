package ast._typesnew.parser;

import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_RIGHT_PAREN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast._typesnew.CFuncParam;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.decl.CDeclEntry;
import ast._typesnew.main.TypeKind;
import ast._typesnew.util.TypeMerger;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.Parse;
import ast.parse.Pskipper;
import jscan.hashed.Hash_ident;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseDecl {
  private final Parse p;

  public ParseDecl(Parse p) {
    this.p = p;
  }

  public CDecl parseDecl() {
    CDecl decl = new CDecl();
    parseDeclInternal(decl);
    return decl;
  }

  private void parseDeclInternal(CDecl out) {
    //    int ns = 0;
    List<Integer> ns2 = new ArrayList<Integer>(0);

    while (p.tok().ofType(T.T_TIMES)) {
      p.move();
      //      ns++;

      Set<Ident> ptrTypeQuals = new HashSet<Ident>();
      while (p.isTypeQual()) {
        Token saved = p.tok();
        p.move();
        ptrTypeQuals.add(saved.getIdent());
      }

      if (!ptrTypeQuals.isEmpty() && ptrTypeQuals.contains(Hash_ident.const_ident)) {
        ns2.add(2);
      } else {
        ns2.add(0);
      }

    }

    parseDirectDeclarator(out);

    while (!ns2.isEmpty()) {
      int p = ns2.remove(0);

      CDeclEntry e = new CDeclEntry(TypeKind.TP_POINTER_TO);
      if (p == 2) {
        e.setConstPointer(true);
      }

      out.add(e);
    }

    //    while (ns-- > 0) {
    //      CDeclEntry e = new CDeclEntry(TypeKind.TP_POINTER_TO);
    //
    //      // TODO: const,volatile pointer alias like __const__ or __const;
    //      // maybe define stubs for all this things.
    //
    //      out.add(e);
    //    }

    new Pskipper(p).skipAttributesAndAsm();

  }

  private void parseDirectDeclarator(CDecl out) {
    if (p.tok().ofType(T.T_LEFT_PAREN)) {
      p.lparen();
      parseDeclInternal(out);
      p.rparen();
    } else if (p.tok().ofType(T.TOKEN_IDENT)) {
      Token saved = p.tok();
      p.move();
      out.setName(saved.getIdent());
    } else {
      //p.perror("no-name");
    }
    while (p.tok().ofType(T.T_LEFT_PAREN) || p.tok().ofType(T.T_LEFT_BRACKET)) {
      Token saved = p.tok();
      p.move();
      if (saved.ofType(T.T_LEFT_PAREN)) {

        CDeclEntry e = new CDeclEntry(TypeKind.TP_FUNCTION);
        List<CFuncParam> params = parseParams(e);
        e.setParameters(params);

        out.add(e);
      } else {
        CDeclEntry e = new CDeclEntry(TypeKind.TP_ARRAY_OF);

        CExpression arrinit = parseArrayInit();

        if (arrinit != null) {
          int arrlen = (int) new ConstexprEval(p).ce(arrinit);
          e.setArrayLen(arrlen);
        }

        out.add(e);
      }
      if (saved.ofType(T.T_LEFT_PAREN)) {
        p.rparen();
      } else {
        p.rbracket();
      }
    }
  }

  private CExpression parseArrayInit() {

    // int x[]
    //       ^
    if (p.tok().ofType(T.T_RIGHT_BRACKET)) {
      return null;
    }

    return new ParseExpression(p).e_expression();
  }

  private List<CFuncParam> parseParams(CDeclEntry e) {
    List<CFuncParam> params = new ArrayList<CFuncParam>();

    // int x()
    //       ^
    if (p.tok().ofType(T.T_RIGHT_PAREN)) {
      return params;
    }

    // check declarations in semantic stage.
    // int x(a,b,c) int a,b,c; {}
    //       ^
    if (!p.isDeclSpecStart() && p.tp() == TOKEN_IDENT) {
      parseIdentifierList(p, params);
      return params;
    }

    CFuncParam param = parseOneParam(e);
    params.add(param);

    while (p.tp() == T.T_COMMA) {
      p.move();

      // int f(char*, ...)
      //
      if (p.tp() == T.T_DOT_DOT_DOT) {

        p.move(); // [...]
        if (!p.tok().ofType(T_RIGHT_PAREN)) {
          p.perror("expect `)` after `...`");
        }

        e.setVariadicFunction(true);
        break;
      }

      CFuncParam paramSeq = parseOneParam(e);
      params.add(paramSeq);
    }

    return params;
  }

  private void parseIdentifierList(Parse p, List<CFuncParam> params) {
    Token id = p.checkedMove(TOKEN_IDENT);
    params.add(new CFuncParam(id.getIdent()));

    while (p.tp() == T.T_COMMA) {
      p.move();
      Token idSeq = p.checkedMove(TOKEN_IDENT);
      params.add(new CFuncParam(idSeq.getIdent()));
    }
  }

  private CFuncParam parseOneParam(CDeclEntry e) {
    CType base = new ParseBase(p).parseBase();
    CDecl decl = parseDecl();

    CType type = TypeMerger.build(base, decl);
    if (decl.isAstract()) {
      return new CFuncParam(type);
    }
    return new CFuncParam(decl.getName(), type);
  }

}
