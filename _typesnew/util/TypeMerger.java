package ast._typesnew.util;

import java.util.ArrayList;
import java.util.List;

import jscan.symtab.Ident;
import jscan.tokenize.Token;
import ast._typesnew.CArrayType;
import ast._typesnew.CFunctionType;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.decl.CDeclEntry;
import ast._typesnew.main.TypeKind;
import ast.parse.ParseException;

public abstract class TypeMerger {

  public static CType build(CType basetype, CDecl declarator) {

    CType tp = basetype;
    if (declarator == null) {
      return tp; // TODO: for unit-tests this ok, but how about reality?
    }

    final List<CDeclEntry> typelist = declarator.getTypelist();
    if (typelist.isEmpty()) {
      return tp;
    }

    List<CType> links = new ArrayList<CType>(0);

    for (int i = typelist.size(); --i >= 0;) {
      CDeclEntry entry = typelist.get(i);
      links.add(0, tp);
      tp = buildFromDeclaratorEntry(entry, links.remove(0));
    }

    return tp;
  }

  private static CType buildFromDeclaratorEntry(CDeclEntry e, CType type) {
    TypeKind base = e.getBase();
    if (base == TypeKind.TP_ARRAY_OF) {
      CArrayType arr = new CArrayType(type, e.getArrlen());
      return new CType(arr, type.getStorage());
    }
    if (base == TypeKind.TP_POINTER_TO) {
      final CType ptrTo = new CType(type, type.getStorage());
      if (e.isConstPointer()) {
        ptrTo.applyTqual(CType.QCONST);
      }
      return ptrTo;
    }
    if (base == TypeKind.TP_FUNCTION) {
      CFunctionType fn = new CFunctionType(type, e.getParameters(), e.isVariadicFunction());
      return new CType(fn, type.getStorage());
    }
    throw new ParseException("build from declarator fail: entry="
        + e.getBase().toString()
        + "; type="
        + type.toString());
  }

  public static void checkTagNotNullForReference(Token tag) {
    if (tag == null) {
      throw new ParseException("for struct/union/enum reference tag must be present always");
    }
  }

  public static Ident getIdentOrNull(Token tag) {
    return tag == null ? null : tag.getIdent();
  }

}
