package ast._typesnew.sem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast._typesnew.CStructField;
import ast._typesnew.util.TypeUtil;
import ast.errors.ParseException;
import ast.parse.NullChecker;
import jscan.symtab.Ident;

public class StructAligner {

  private final boolean isUnion;
  private final boolean isReference;
  private final List<CStructField> fields;

  private int size;
  private int align;

  public StructAligner(boolean isUnion, boolean isReference, List<CStructField> fields) {
    this.isUnion = isUnion;
    this.isReference = isReference;

    if (!isReference) {
      NullChecker.check(fields);
    }

    this.fields = fields;
    this.size = 0;
    this.align = 1;

    if (isReference) {
      // TODO:
      //System.out.println("struct ref...TODO...");
    }

    else {

      checkFieldsUnique();
      applyAlignment();
      if (isUnion) {
        calcUnionFieldsOffsets();
      } else {
        calcStructFieldsOffsets();
      }
    }
  }

  private void applyAlignment() {
    for (CStructField f : fields) {
      align = TypeUtil.align(align, f.getType().getAlign());
    }
  }

  private void checkFieldsUnique() {
    Set<Ident> toCheckUnique = new HashSet<Ident>();
    for (CStructField f : fields) {
      if (f.isHasName()) {
        final Ident name = f.getName();
        if (toCheckUnique.contains(name)) {
          throw new ParseException("duplicate struct/union field: " + name.getName());
        }
        toCheckUnique.add(name);
      }
    }
  }

  private int getMaxFieldSize(List<CStructField> fields) {
    int msize = 0;
    for (CStructField f : fields) {
      final int size = f.getType().getSize();
      if (msize < size) {
        msize = size;
      }
    }
    if (msize == 0) {
      throw new ParseException("internal error: zero sized field...");
    }
    return msize;
  }

  private void calcStructFieldsOffsets() {
    int offset = 0;
    for (CStructField f : fields) {
      offset = TypeUtil.align(offset, f.getType().getAlign());
      f.setOffset(offset);
      offset += f.getType().getSize();
    }
    size = offset;
  }

  private void calcUnionFieldsOffsets() {
    size = getMaxFieldSize(fields);
    for (CStructField f : fields) {
      f.setOffset(0);
    }
  }

  public int getSize() {
    return size;
  }

  public int getAlign() {
    return align;
  }

}