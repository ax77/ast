package ast._typesnew.parser;

import static jscan.tokenize.T.TOKEN_IDENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast._typesnew.CEnumType;
import ast._typesnew.CStructType;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.sem.CStructUnionSizeAlign;
import ast._typesnew.sem.SemanticStruct;
import ast._typesnew.util.TypeCombiner;
import ast.parse.Parse;
import ast.parse.ParseState;
import ast.parse.Pcheckers;
import ast.symtabg.elements.CSymbol;
import jscan.hashed.Hash_ident;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseBase {
  private final Parse p;

  // declaration-specifiers flags
  // combinations:
  // type-qualifier-list
  // specifier-qualifier-list 
  //
  private final int f_expect_storage_class = 1;
  private final int f_expect_typespec = 2;
  private final int f_expect_typequal = 4;
  private final int f_expect_funcspec = 8;

  public ParseBase(Parse p) {
    this.p = p;
  }

  private String look(int howMuch) {
    StringBuilder sb = new StringBuilder();
    ParseState parseState = new ParseState(p);
    for (int i = 0; i < howMuch; i++) {
      Token t = p.tok();
      if (t.ofType(T.TOKEN_EOF)) {
        break;
      }
      p.move();
      if (t.hasLeadingWhitespace()) {
        sb.append(" ");
      }
      sb.append(t.getValue());
    }
    p.restoreState(parseState);
    return sb.toString();
  }

  private int getQual(Set<Token> tq) {
    int f = 0;
    for (Token t : tq) {
      if (Pcheckers.isConstIdent(t)) {
        f |= CType.QCONST;
      }
    }
    return f;
  }

  private int getFspec(Set<Token> fs) {
    int f = 0;
    for (Token t : fs) {
      if (Pcheckers.isInlineIdent(t)) {
        f |= CType.FINLIN;
      }
      if (Pcheckers.isNoreturnIdent(t)) {
        f |= CType.FNORET;
      }
    }
    return f;
  }

  private CType findType() {
    CType basetype = null;

    List<Token> st = new ArrayList<Token>();
    Set<Token> tq = new HashSet<Token>();
    Set<Token> fs = new HashSet<Token>();
    List<Token> ts = new ArrayList<Token>();
    tail(st, ts, tq, fs, true);

    ParseState parseState = new ParseState(p);
    Token curtok = p.tok();

    boolean isMaybeTypedef = curtok.ofType(TOKEN_IDENT) && !curtok.isBuiltinIdent() && ts.isEmpty();

    if (isMaybeTypedef) {
      Ident symname = curtok.getIdent();
      CSymbol symbol = p.getSym(symname);
      if (symbol != null) {
        CType typeFromStab = symbol.getType();
        if (typeFromStab.getStorage() == StorageKind.ST_TYPEDEF) {

          // if all ok, we eat this typedefed-name, and assign it's type to basetype
          // XXX: storage-class _ALWAYS_ build from 'st'
          // for this, we copy typedefed-type, with new storage-class we have now...
          p.move();

          tail(st, ts, tq, fs, true);
          StorageKind storagespec = TypeCombiner.combine_storage(st);
          basetype = new CType(typeFromStab, storagespec, getQual(tq), getFspec(fs));

        } else {
          p.restoreState(parseState);
        }
      } else {
        p.restoreState(parseState);
      }
    }

    else if (Pcheckers.isStructOrUnionSpecStart(curtok)) {
      if (!ts.isEmpty()) {
        p.perror("type already recognized...");
      }
      p.move();
      boolean isUnion = (curtok.isIdent(Hash_ident.union_ident));

      CStructType str = new ParseStruct(p).parseStruct(isUnion);
      CStructUnionSizeAlign sizeAlignDto = new SemanticStruct(p).finalizeStructType(str);

      StorageKind storagespec = TypeCombiner.combine_storage(st);
      basetype = new CType(str, sizeAlignDto.getSize(), sizeAlignDto.getAlign(), storagespec);

    }

    else if (Pcheckers.isEnumSpecStart(curtok)) {
      if (!ts.isEmpty()) {
        p.perror("type already recognized...");
      }
      p.move();

      CEnumType en = new ParseEnum(p).parseEnum();
      StorageKind storagespec = TypeCombiner.combine_storage(st);
      basetype = new CType(en, storagespec);
    }

    StorageKind storagespec = TypeCombiner.combine_storage(st);
    if (basetype == null) {
      if (ts.isEmpty()) {
        p.pwarning("empty type-specifier. default INT.");
        basetype = new CType(TypeKind.TP_INT, storagespec);
      } else {
        TypeKind bts = TypeCombiner.combine_typespec(ts);
        basetype = new CType(bts, storagespec);
      }
    }

    basetype.applyTqual(getQual(tq));
    basetype.applyFspec(getFspec(fs));
    return basetype;
  }

  public CType parseBase() {

    return findType();

    //    List<Token> st = new ArrayList<Token>();
    //    Set<Token> tq = new HashSet<Token>();
    //    Set<Token> fs = new HashSet<Token>();
    //    List<Token> ts = new ArrayList<Token>();
    //
    //    tail(st, ts, tq, fs, true);
    //
    //    boolean fromtypedef = false;
    //    CType typespec = null;
    //    Ident symname = null;
    //    boolean isPlainIdent = p.tok().ofType(TOKEN_IDENT) && !p.tok().getIdent().isBuiltin();
    //
    //    if (isPlainIdent) {
    //      Token saved = p.tok();
    //
    //      symname = saved.getIdent();
    //      if (p.isTypedefName(symname)) {
    //        if (ts.isEmpty()) {
    //          p.move();
    //        }
    //        tail(st, ts, tq, fs, false);
    //        fromtypedef = true;
    //      }
    //    }
    //
    //    else {
    //
    //      if (p.isStructOrUnionSpecStart()) {
    //
    //        Token saved = p.tok();
    //        p.move();
    //
    //        boolean isUnion = (saved.isIdent(Hash_ident.union_ident));
    //
    //        CStructType str = new ParseStruct(p).parseStruct(isUnion);
    //        CStructUnionSizeAlign sizeAlignDto = new SemanticStruct(p).finalizeStructType(str);
    //
    //        StorageKind storagespec = TypeCombiner.combine_storage(st);
    //        typespec = new CType(str, sizeAlignDto.getSize(), sizeAlignDto.getAlign(), storagespec);
    //
    //      }
    //
    //      else if (p.isEnumSpecStart()) {
    //        if (!ts.isEmpty()) {
    //          p.perror("type recognize...");
    //        }
    //
    //        if (!ts.isEmpty()) {
    //          p.perror("type already recognized...");
    //        }
    //
    //        Token saved = p.tok();
    //        p.move();
    //
    //        CEnumType en = new ParseEnum(p).parseEnum();
    //
    //        if (!en.isReference()) {
    //          new SemanticEnum(p).finalizeEnumerators(en.getEnumerators());
    //        }
    //
    //        StorageKind storagespec = TypeCombiner.combine_storage(st);
    //        typespec = new CType(en, storagespec);
    //
    //      }
    //
    //      else {
    //        if (ts.isEmpty()) {
    //          p.perror("can't find the type in decl-specs");
    //        }
    //
    //        TypeKind bts = TypeCombiner.combine_typespec(ts);
    //        StorageKind storagespec = TypeCombiner.combine_storage(st);
    //
    //        typespec = new CType(bts, storagespec);
    //      }
    //
    //    }
    //
    //    StorageKind storagespec = TypeCombiner.combine_storage(st);
    //
    //    if (typespec == null) {
    //      if (fromtypedef) {
    //        if (!ts.isEmpty()) {
    //          TypeKind bts = TypeCombiner.combine_typespec(ts);
    //          typespec = new CType(bts, storagespec);
    //        } else {
    //          CSymbol sym = p.getSym(symname);
    //          if (sym == null) {
    //            p.perror("no sym found.");
    //          } else {
    //            typespec = sym.getType();
    //          }
    //        }
    //      } else {
    //        if (ts.isEmpty()) {
    //          p.pwarning("empty type-specifier. default INT.");
    //          typespec = new CType(TypeKind.TP_INT, storagespec);
    //        } else {
    //          TypeKind bts = TypeCombiner.combine_typespec(ts);
    //          typespec = new CType(bts, storagespec);
    //        }
    //      }
    //    }
    //
    //    if (typespec == null) {
    //      p.perror("no type-spec at all... paranoia... ");
    //    }
    //
    //    return typespec;
  }

  private void tail(List<Token> st, List<Token> ts, Set<Token> tq, Set<Token> fs, boolean allowTS) {
    for (;;) {
      if (p.isStorageClassSpec()) {
        Token saved = p.tok();
        p.move();
        st.add(saved);
      } else if (p.isTypeSpec() && allowTS) {
        Token saved = p.tok();
        p.move();
        ts.add(saved);
      } else if (p.isTypeQual()) {
        Token saved = p.tok();
        p.move();
        tq.add(saved);
      } else if (p.isFuncSpec()) {
        Token saved = p.tok();
        p.move();
        fs.add(saved);
      } else {
        break;
      }
    }
  }

  // TODO:

  private void checkds(int variant, List<Token> st, List<Token> ts, Set<Token> tq, Set<Token> fs,
      StringBuilder buffer) {
    boolean expectTypeSpec = (variant & f_expect_typespec) == f_expect_typespec;
    boolean expectStorage = (variant & f_expect_storage_class) == f_expect_storage_class;
    boolean expectTypeQual = (variant & f_expect_typequal) == f_expect_typequal;
    boolean expectFuncSpec = (variant & f_expect_funcspec) == f_expect_funcspec;

    if (!expectStorage && !st.isEmpty()) {
      p.perror("storage-class-specifiers not expect in this context: " + buffer.toString());
    }

    if (!expectTypeSpec && !ts.isEmpty()) {
      p.perror("type-specifiers not expect in this context: " + buffer.toString());
    }

    if (!expectTypeQual && !tq.isEmpty()) {
      p.perror("type-qualifiers not expect in this context: " + buffer.toString());
    }

    if (!expectFuncSpec && !fs.isEmpty()) {
      p.perror("function-specifiers not expect in this context: " + buffer.toString());
    }
  }

}
