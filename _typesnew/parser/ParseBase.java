package ast._typesnew.parser;

import static jscan.tokenize.T.TOKEN_IDENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast._typesnew.CType;
import ast._typesnew.CTypeImpl;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.util.TypeCombiner;
import ast.parse.Parse;
import ast.symtabg.elements.CSymbol;
import ast.symtabg.elements.CSymbolBase;
import jscan.hashed.Hash_ident;
import jscan.tokenize.Token;

public class ParseBase {
  private final Parse p;
  private StorageKind storageSpec;

  public ParseBase(Parse p) {
    this.p = p;
    this.storageSpec = StorageKind.ST_NONE;
  }

  public CType parseBase() {
    return findTypeAgain();
  }

  //////////////////////////////////////////////////////////////////////

  public CType findTypeAgain() {

    List<Token> storage = new ArrayList<Token>();
    List<Token> compoundKeywords = new ArrayList<Token>();
    Set<Token> qualifiers = new HashSet<Token>();
    cut(storage, compoundKeywords, qualifiers);

    // new storage present always...
    storageSpec = TypeCombiner.combine_storage(storage);

    // const typedef struct x tdname;
    // typedef const struct x tdname;
    // struct ...
    // enum  ...
    // union ...

    // if found struct/union/enum with|without typedef: one case
    // if not found: another case
    // if found typedefed-alias from symtab: another-another case
    // if found one more one variant: another-another-another case ... 

    // 1) compound
    if (!compoundKeywords.isEmpty()) {
      Token first = compoundKeywords.remove(0);
      if (first.isIdent(Hash_ident.enum_ident)) {
        CType ty = new ParseEnum(p).parseEnum(storageSpec);
        return ty;
      }

      else {
        boolean isUnion = (first.isIdent(Hash_ident.union_ident));
        CType ty = new ParseStruct(p).parseStruct(isUnion, storageSpec);
        //        ty.setStorage(storagespec);
        return ty;
      }
    }

    if (p.isTypeSpec()) {
      // int typedef i32;
      // int x;
      // int const static x;
      // int const typedef i32;
      // ... ... ...
      //

      List<Token> ts = new ArrayList<Token>();
      cut2(storage, ts, qualifiers);
      storageSpec = TypeCombiner.combine_storage(storage);

      TypeKind bts = TypeCombiner.combine_typespec(ts);
      CType basetype = new CType(bts);

      return basetype;
    }

    // if we here: it guarantee us that the typedef-name must be present.
    // because if we are here: we still not found the type...
    // but: it also may be a typedef-redeclaration (1) or typedef-usage (2):
    // 1) i32 typedef i32;
    // 2) i32 varname;
    // i32 int ... :: error
    //

    if (p.tok().ofType(TOKEN_IDENT) && !p.tok().isBuiltinIdent()) {
      CSymbol symbol = p.getSym(p.tok().getIdent());
      if (symbol != null) {
        CType typeFromStab = symbol.getType();
        if (symbol.getBase() == CSymbolBase.SYM_TYPEDEF) {
          p.move();

          List<Token> ts = new ArrayList<Token>();
          cut2(storage, ts, qualifiers);
          if (!ts.isEmpty()) {
            p.perror("error_1");
          }

          storageSpec = TypeCombiner.combine_storage(storage);
          return typeFromStab;
        }
      }
    }

    // 'int' by default
    p.pwarning("default type-int... if type not specified.");
    return CTypeImpl.TYPE_INT;

    //    p.perror("error_2");
    //    return null;
  }

  private void cut2(List<Token> st, List<Token> ts, Set<Token> tq) {
    for (;;) {
      if (p.isStorageClassSpec()) {
        Token saved = p.tok();
        p.move();
        st.add(saved);
      }

      else if (p.isTypeSpec()) {
        Token saved = p.tok();
        p.move();
        ts.add(saved);
      }

      else if (p.isTypeQual()) {
        Token saved = p.tok();
        p.move();
        tq.add(saved);
      }

      else if (p.isFuncSpec()) {
        Token saved = p.tok();
        p.move();
        //              fs.add(saved);
      }

      else {
        break;
      }
    }
  }

  private void cut(List<Token> storage, List<Token> compoundKeywords, Set<Token> qualifiers) {
    while (!p.isEof()) {

      if (p.isStorageClassSpec()) {
        Token saved = p.tok();
        p.move();
        storage.add(saved);
      }

      else if (p.isTypeQual()) {
        Token saved = p.tok();
        p.move();
        qualifiers.add(saved);
      }

      else if (p.isFuncSpec()) {
        Token saved = p.tok();
        p.move();
        //        fs.add(saved);
      }

      else if (p.isStructOrUnionSpecStart() || p.isEnumSpecStart()) {
        Token saved = p.tok();
        p.move();
        compoundKeywords.add(saved);
        break; // XXX: nothing else.
      }

      else {
        break;
      }
    }
  }

  public StorageKind getStorageSpec() {
    return storageSpec;
  }

  public void setStorageSpec(StorageKind storageSpec) {
    this.storageSpec = storageSpec;
  }

}
