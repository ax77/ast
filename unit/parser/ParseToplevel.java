package ast.unit.parser;

import static jscan.tokenize.T.T_LEFT_BRACE;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.decls.Declaration;
import ast.decls.Initializer;
import ast.decls.parser.ParseDeclarations;
import ast.parse.Parse;
import ast.stmt.main.CStatement;
import ast.stmt.parser.ParseStatement;
import ast.symtab.elements.CSymbol;
import ast.symtab.elements.CSymbolBase;
import ast.types.CFuncParam;
import ast.types.CType;
import ast.types.decl.CDecl;
import ast.types.main.StorageKind;
import ast.types.parser.ParseBase;
import ast.types.parser.ParseDecl;
import ast.types.util.TypeMerger;
import ast.unit.ExternalDeclaration;
import ast.unit.FunctionDefinition;

public class ParseToplevel {

  private final Parse parser;

  public ParseToplevel(Parse parser) {
    this.parser = parser;
  }

  public ExternalDeclaration parseExternalDeclaration() {

    ParseBase pb = new ParseBase(parser);
    CType declspecs = pb.findTypeAgain();
    StorageKind storageSpec = pb.getStorageSpec();

    if (parser.tp() == T_SEMI_COLON) {
      boolean isStructUnionEnum = declspecs.isStrUnion() || declspecs.isEnumeration();
      if (!isStructUnionEnum) {
        parser.perror("strange declaration-specifiers: " + declspecs.toString());
      }
      Declaration declaration = new Declaration(parser.tok(), parser.tok(), declspecs); // TODO:pos

      parser.semicolon(); // XXX:
      return new ExternalDeclaration(declaration);
    }

    CDecl declarator = new ParseDecl(parser).parseDecl();
    CType type = TypeMerger.build(declspecs, declarator);

    // if we here -> the next may one of
    // 1) KnR declarations
    // 2) {
    // 3) ;
    // 4) , for this: int f(void), *fip(), (*pfi)(), *ap[3];

    boolean isDeclstartOrSemicolonOrLbraceOrComma = parser.tp() == T.T_SEMI_COLON
        || parser.tp() == T.T_LEFT_BRACE
        || parser.tp() == T.T_COMMA
        || parser.tp() == T.T_ASSIGN
        || parser.isDeclSpecStart();

    if (!isDeclstartOrSemicolonOrLbraceOrComma) {
      parser.perror("strange declarator: " + type.toString());
    }

    Token current = parser.tok();

    // int x;
    // .....^
    if (current.ofType(T_SEMI_COLON)) {
      CSymbolBase base = CSymbolBase.SYM_LVAR;
      if (storageSpec == StorageKind.ST_TYPEDEF) {
        base = CSymbolBase.SYM_TYPEDEF;
      }
      CSymbol sym = new CSymbol(base, declarator.getName(), type, current);
      parser.defineSym(declarator.getName(), sym);

      List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

      initDeclaratorList.add(sym);
      Declaration declaration = new Declaration(current, current, initDeclaratorList);

      parser.semicolon(); // XXX:
      return new ExternalDeclaration(declaration);
    }

    if (current.ofType(T.T_COMMA) || current.ofType(T.T_ASSIGN)) {

      if (current.ofType(T.T_COMMA)) {

        List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

        // head
        CSymbolBase base = CSymbolBase.SYM_LVAR;
        if (storageSpec == StorageKind.ST_TYPEDEF) {
          base = CSymbolBase.SYM_TYPEDEF;
        }
        CSymbol sym = new CSymbol(base, declarator.getName(), type, current);
        parser.defineSym(declarator.getName(), sym);
        initDeclaratorList.add(sym);

        // tail from 'parse-declaration'
        //
        while (parser.tp() == T.T_COMMA) {
          parser.move();
          CSymbol initDeclaratorSeq = new ParseDeclarations(parser, declspecs, storageSpec).parseInitDeclarator();
          initDeclaratorList.add(initDeclaratorSeq);
        }

        Declaration declaration = new Declaration(current, current, initDeclaratorList);

        parser.semicolon(); // XXX:
        return new ExternalDeclaration(declaration);

      }

      else {

        List<CSymbol> initDeclaratorList = new ArrayList<CSymbol>(0);

        // initializer's
        //

        parser.checkedMove(T.T_ASSIGN);
        List<Initializer> initializer = new ParseDeclarations(parser, type, storageSpec).parseInitializer(type);

        if (storageSpec == StorageKind.ST_TYPEDEF) {
          parser.perror("typedef with initializer.");
        }

        CSymbol sym = new CSymbol(CSymbolBase.SYM_LVAR, declarator.getName(), type, initializer, current);
        parser.defineSym(declarator.getName(), sym);

        initDeclaratorList.add(sym);

        // tail from 'parse-declaration'
        //
        while (parser.tp() == T.T_COMMA) {
          parser.move();
          CSymbol initDeclaratorSeq = new ParseDeclarations(parser, declspecs, storageSpec).parseInitDeclarator();
          initDeclaratorList.add(initDeclaratorSeq);
        }

        Declaration declaration = new Declaration(current, current, initDeclaratorList);

        parser.semicolon(); // XXX:
        return new ExternalDeclaration(declaration);
      }

    }

    //////////////////////////////////////////////////////////////////////
    //
    // normal cases for function begin here

    return functionDef(declarator, type);

  }

  public ExternalDeclaration functionDef(CDecl declarator, CType type) {
    // K&R function style declaration-list
    //
    if (parser.isDeclSpecStart()) {
      parser.perror("unimpl. KnR function declaration.");
    }

    // and corner case: ANSI function-definition
    //
    if (parser.tp() != T_LEFT_BRACE || !type.isFunction()) {
      parser.perror("expect function definition...");
    }

    CSymbol funcSymbol = new CSymbol(CSymbolBase.SYM_FUNC, declarator.getName(), type, parser.tok());
    parser.defineSym(declarator.getName(), funcSymbol);
    FunctionDefinition fd = new FunctionDefinition(funcSymbol);

    parser.setCurrentFn(fd);
    parser.pushscope();

    defineParameters(fd.getSignature().getType());
    define__func__(fd.getSymbol().getName());

    CStatement cst = new ParseStatement(parser).parseCompoundStatement(true);
    fd.setCompoundStatement(cst);

    parser.setCurrentFn(null);
    parser.popscope();

    return new ExternalDeclaration(fd);
  }

  // TODO:
  private void define__func__(Ident funcName) {
  }

  private void defineParameters(CType signature) {

    final List<CFuncParam> parameters = signature.getTpFunction().getParameters();

    if (parameters.size() == 1) {
      CFuncParam first = parameters.get(0);
      if (first.getType().isVoid() && first.getName() == null) {
        return;
      }
    }

    for (CFuncParam fparam : parameters) {
      CSymbol paramsym = new CSymbol(CSymbolBase.SYM_LVAR, fparam.getName(), fparam.getType(), parser.tok());
      parser.defineSym(fparam.getName(), paramsym);
    }
  }

}
