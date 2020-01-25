package ast.parse;

import static jscan.tokenize.T.T_LEFT_BRACE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ast._typesnew.CArrayType;
import ast._typesnew.CFuncParam;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.decl.CDeclEntry;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.parser.ParseBase;
import ast._typesnew.parser.ParseDecl;
import ast._typesnew.util.TypeMerger;
import ast.declarations.Initializer;
import ast.declarations.main.Declaration;
import ast.declarations.parser.ParseDeclarations;
import ast.expr.main.CExpression;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.CSymbolBase;
import ast.unit.FunctionDefinition;
import jscan.hashed.Hash_ident;
import jscan.symtab.Ident;
import jscan.tokenize.T;

public class Pfunction {

  private final Parse p;

  public Pfunction(Parse p) {
    this.p = p;
  }

  public FunctionDefinition isNextFunctionDefinition() {
    ParseState state = new ParseState(p);

    CType declspecs = new ParseBase(p).parseBase();

    // struct/union/enum type-specifiers
    // check this
    //    if (p.tp() == T.T_SEMI_COLON) {
    //      CTypeSpec typespec = declspecs.getTypespec();
    //      if (typespec == null) {
    //        p.perror("no type specifiers...");
    //      }
    //      CTypeSpecBase base = typespec.getBase();
    //      if (base == CTypeSpecBase.TS_STRUCT_UNION || base == CTypeSpecBase.TS_ENUM) {
    //        p.restoreState(state);
    //        return null;
    //      }
    //    }

    CDecl decl = new ParseDecl(p).parseDecl();
    CType signature = TypeMerger.build(declspecs, decl);

    if (!signature.isFunction() || decl.isAstract()) {
      p.restoreState(state);
      return null;
    }

    // if we here -> the next may one of
    // 1) KnR declarations
    // 2) {
    // 3) ;
    // 4) , for this: int f(void), *fip(), (*pfi)(), *ap[3];

    //@formatter:off
    boolean isDeclstartOrSemicolonOrLbraceOrComma = 
           p.tp() == T.T_SEMI_COLON
        || p.tp() == T.T_LEFT_BRACE
        || p.tp() == T.T_COMMA
        || p.isDeclSpecStart();
    
    if (!isDeclstartOrSemicolonOrLbraceOrComma) {
      p.perror("strange declarator: " + decl.toString());
    }
    //@formatter:on

    // 1) function prototype
    // 2) declarations separated by comma
    if (p.tp() == T.T_SEMI_COLON || p.tp() == T.T_COMMA) {
      p.restoreState(state);
      return null;
    }

    // normal cases begin here

    // K&R function style declaration-list
    //
    if (p.isDeclSpecStart()) {

      List<Declaration> decllist = new ArrayList<Declaration>();

      Declaration onedec = new ParseDeclarations(p).parseDeclaration();
      decllist.add(onedec);

      while (p.isDeclSpecStart()) {
        onedec = new ParseDeclarations(p).parseDeclaration();
        decllist.add(onedec);
      }

      if (p.tp() != T_LEFT_BRACE) {
        p.perror("expect function definition...");
      }

      // K&R function definition
      //
      alllyTypes(decllist, decl);

      CType newsign = TypeMerger.build(declspecs, decl);
      CSymbol sym = new CSymbol(CSymbolBase.SYM_FUNC, decl.getName(), newsign, p.tok());
      p.defineSym(decl.getName(), sym);
      define__func__(decl.getName());

      defineParameters(newsign);
      return new FunctionDefinition(sym);

    }

    // and corner case: ANSI function-definition
    //
    if (p.tp() != T_LEFT_BRACE) {
      p.perror("expect function definition...");
    }

    CSymbol sym = new CSymbol(CSymbolBase.SYM_FUNC, decl.getName(), signature, p.tok());
    p.defineSym(decl.getName(), sym);
    define__func__(decl.getName());

    defineParameters(signature);
    return new FunctionDefinition(sym);
  }

  private void define__func__(Ident funcName) {
    CType chartype = new CType(TypeKind.TP_UCHAR, StorageKind.ST_STATIC);
    CArrayType arr = new CArrayType(chartype, funcName.getName().length() + 1);
    final Ident __func__ = Hash_ident.__func___ident;

    CSymbol sym = new CSymbol(CSymbolBase.SYM_VAR, __func__, new CType(arr, StorageKind.ST_STATIC), p.tok());
    CExpression init = new CExpression(funcName.getName(), p.tok());
    sym.setInitializer(new Initializer(init));

    p.defineSym(__func__, sym);
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
      CSymbol paramsym = new CSymbol(CSymbolBase.SYM_VAR, fparam.getName(), fparam.getType(), p.tok());
      p.defineSym(fparam.getName(), paramsym);
    }
  }

  private void assertTrue(boolean what) {
    if (!what) {
      p.perror("assertion fail.");
    }
  }

  private void alllyTypes(List<Declaration> decllist, CDecl decl) {
    final List<CDeclEntry> typelist = decl.getTypelist();
    assertTrue(typelist.size() == 1);
    assertTrue(typelist.get(0).getBase() == TypeKind.TP_FUNCTION);
    List<CFuncParam> parameters = typelist.get(0).getParameters();

    Map<Ident, CType> vars = new HashMap<Ident, CType>(0);
    for (Declaration declaration : decllist) {
      if (!declaration.isVarlist()) {
        p.perror("expect variables declaration for old-style function-definition.");
      }
      for (CSymbol id : declaration.getVariables()) {
        final Ident name = id.getName();
        if (vars.containsKey(name)) {
          p.perror("declaration name duplicate: " + name.getName()); // TODO: not here.
        }
        vars.put(name, id.getType());
      }
    }

    if (vars.size() != parameters.size()) {
      p.perror("different size between parameters and declarations.");
    }

    for (CFuncParam p : typelist.get(0).getParameters()) {
      CType tp = vars.get(p.getName());
      p.setType(tp);
    }
  }

}
