package ast._typesnew.util;

import static jscan.hashed.Hash_ident._Bool_ident;
import static jscan.hashed.Hash_ident._Complex_ident;
import static jscan.hashed.Hash_ident._Imaginary_ident;
import static jscan.hashed.Hash_ident.auto_ident;
import static jscan.hashed.Hash_ident.char_ident;
import static jscan.hashed.Hash_ident.double_ident;
import static jscan.hashed.Hash_ident.extern_ident;
import static jscan.hashed.Hash_ident.float_ident;
import static jscan.hashed.Hash_ident.int_ident;
import static jscan.hashed.Hash_ident.long_ident;
import static jscan.hashed.Hash_ident.register_ident;
import static jscan.hashed.Hash_ident.short_ident;
import static jscan.hashed.Hash_ident.signed_ident;
import static jscan.hashed.Hash_ident.static_ident;
import static jscan.hashed.Hash_ident.typedef_ident;
import static jscan.hashed.Hash_ident.unsigned_ident;
import static jscan.hashed.Hash_ident.void_ident;

import java.util.List;

import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast.parse.ParseException;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class TypeCombiner {

  public static StorageKind combine_storage(List<Token> list) {

    if (list.isEmpty()) {
      return StorageKind.ST_NONE;
    }

    //@formatter:off
    final int f_typedef   = 1 << 0;
    final int f_extern    = 1 << 1;
    final int f_static    = 1 << 2;
    final int f_auto      = 1 << 3;
    final int f_register  = 1 << 4;
    //@formatter:on

    int storages = 0;
    int flag = 0;

    StringBuilder ts = new StringBuilder();
    String lastLoc = "";

    for (Token tok : list) {
      if (!tok.ofType(T.TOKEN_IDENT)) {
        throw new ParseException(tok.loc() + " error: expect id, but was: " + tok.getValue());
      }
      Ident id = tok.getIdent();
      lastLoc = tok.loc();
      ts.append(id.getName() + " ");

      if (tok.isIdent(typedef_ident)) {
        checkzero(storages, lastLoc, "multiple storage class specifier");
        flag |= f_typedef;
        storages += 1;
      }

      else if (tok.isIdent(extern_ident)) {
        checkzero(storages, lastLoc, "multiple storage class specifier");
        flag |= f_extern;
        storages += 1;
      }

      else if (tok.isIdent(static_ident)) {
        checkzero(storages, lastLoc, "multiple storage class specifier");
        flag |= f_static;
        storages += 1;
      }

      else if (tok.isIdent(auto_ident)) {
        checkzero(storages, lastLoc, "multiple storage class specifier");
        flag |= f_auto;
        storages += 1;
      }

      else if (tok.isIdent(register_ident)) {
        checkzero(storages, lastLoc, "multiple storage class specifier");
        flag |= f_register;
        storages += 1;
      }

      else {
        throw new ParseException(tok.loc() + "error: expect storage-class-specifier, but was: " + tok.getValue());
      }
    }

    if ((flag & f_typedef) == f_typedef) {
      return StorageKind.ST_TYPEDEF;
    }
    if ((flag & f_extern) == f_extern) {
      return StorageKind.ST_EXTERN;
    }
    if ((flag & f_static) == f_static) {
      return StorageKind.ST_STATIC;
    }
    if ((flag & f_auto) == f_auto) {
      return StorageKind.ST_AUTO;
    }
    if ((flag & f_register) == f_register) {
      return StorageKind.ST_REGISTER;
    }

    throw new ParseException(lastLoc + "error:" + "unknown storage-class-specifier [" + ts.toString() + "]");
  }

  public static TypeKind combine_typespec(List<Token> list) {

    //@formatter:off
    final int f_void        = 1 << 0 ;
    final int f_bool        = 1 << 1 ;
    final int f_char        = 1 << 2 ;
    final int f_int         = 1 << 3 ;
    final int f_signed      = 1 << 4 ;
    final int f_unsigned    = 1 << 5 ;
    final int f_short       = 1 << 6 ;
    final int f_long        = 1 << 7 ;
    final int f_longlong    = 1 << 8 ;
    final int f_float       = 1 << 9 ;
    final int f_double      = 1 << 10 ;
    final int f_complex     = 1 << 11 ;
    final int f_imaginary   = 1 << 12 ;
    //@formatter:on  

    int void_cnt = 0;
    int bool_cnt = 0;
    int char_cnt = 0;
    int signed_cnt = 0;
    int unsigned_cnt = 0;
    int short_cnt = 0;
    int int_cnt = 0;
    int long_cnt = 0;
    int float_cnt = 0;
    int double_cnt = 0;
    int complex_cnt = 0;
    int imaginary_cnt = 0;

    int flag = 0;

    StringBuilder ts = new StringBuilder();
    String lastLoc = "";

    for (Token tok : list) {
      if (!tok.ofType(T.TOKEN_IDENT)) {
        throw new ParseException(tok.loc() + " error: expect id, but was: " + tok.getValue());
      }
      Ident id = tok.getIdent();
      lastLoc = tok.loc();
      ts.append(id.getName() + " ");

      if (id == void_ident) {
        checkdup(void_cnt, 1, lastLoc, "void");
        void_cnt += 1;
        flag |= f_void;
      }

      else if (id == _Bool_ident) {
        checkdup(bool_cnt, 1, lastLoc, "bool");
        bool_cnt += 1;
        flag |= f_bool;
      }

      else if (id == char_ident) {
        checkdup(char_cnt, 1, lastLoc, "char");
        char_cnt += 1;
        flag |= f_char;
      }

      else if (id == signed_ident) {
        checkzero(unsigned_cnt, lastLoc, "signed with unsigned is mistake");
        checkdup(signed_cnt, 1, lastLoc, "signed");
        signed_cnt += 1;
        flag |= f_signed;
      }

      else if (id == unsigned_ident) {
        checkzero(signed_cnt, lastLoc, "signed with unsigned is mistake");
        checkdup(unsigned_cnt, 1, lastLoc, "unsigned");
        unsigned_cnt += 1;
        flag |= f_unsigned;
      }

      else if (id == short_ident) {
        checkdup(short_cnt, 1, lastLoc, "short");
        short_cnt += 1;
        flag |= f_short;
      }

      else if (id == int_ident) {
        checkdup(int_cnt, 1, lastLoc, "int");
        int_cnt += 1;
        flag |= f_int;
      }

      else if (id == long_ident) {
        checkdup(long_cnt, 2, lastLoc, "long");

        if (long_cnt == 1) { // is long long
          flag &= ~f_long;
          flag |= f_longlong;
        } else {
          flag |= f_long;
        }
        long_cnt += 1;
      }

      else if (id == float_ident) {
        checkdup(float_cnt, 1, lastLoc, "float");
        float_cnt += 1;
        flag |= f_float;
      }

      else if (id == double_ident) {
        checkdup(double_cnt, 1, lastLoc, "double");
        double_cnt += 1;
        flag |= f_double;
      }

      else if (id == _Complex_ident) {
        checkdup(complex_cnt, 1, lastLoc, "complex");
        complex_cnt += 1;
        flag |= f_complex;
      }

      else if (id == _Imaginary_ident) {
        checkdup(imaginary_cnt, 1, lastLoc, "imaginary");
        imaginary_cnt += 1;
        flag |= f_imaginary;
      }

      else {
        throw new ParseException(tok.loc() + "error: expect type-specifier, but was: " + tok.getValue());
      }

    }

    // void
    boolean isvoid = istype(flag, f_void, 0);
    if (isvoid) {
      return TypeKind.TP_VOID;
    }

    // _Bool
    boolean isbool = istype(flag, f_bool, 0);
    if (isbool) {
      return TypeKind.TP_BOOL;
    }

    // char
    // signed char
    boolean ischar = istype(flag, f_char, f_signed);
    if (ischar) {
      return TypeKind.TP_CHAR;
    }

    // unsigned char
    boolean isuchar = istype(flag, f_char | f_unsigned, 0);
    if (isuchar) {
      return TypeKind.TP_UCHAR;
    }

    // short
    // short int
    // signed short
    // signed short int
    boolean isshort = istype(flag, f_short, f_int | f_signed);
    if (isshort) {
      return TypeKind.TP_SHORT;
    }

    // unsigned short
    // unsigned short int
    boolean isushort = istype(flag, f_short | f_unsigned, f_int);
    if (isushort) {
      return TypeKind.TP_USHORT;
    }

    // int 
    // signed
    // signed int
    boolean isint = istype(flag, f_int, f_signed) || istype(flag, f_signed, f_int);
    if (isint) {
      return TypeKind.TP_INT;
    }

    // unsigned
    // unsigned int
    boolean isuint = istype(flag, f_int | f_unsigned, 0) || istype(flag, f_unsigned, 0);
    if (isuint) {
      return TypeKind.TP_UINT;
    }

    // long
    // long int
    // signed long
    // signed long int
    boolean islong = istype(flag, f_long, f_int | f_signed);
    if (islong) {
      return TypeKind.TP_LONG;
    }

    // unsigned long
    // unsigned long int
    boolean isulong = istype(flag, f_long | f_unsigned, f_int);
    if (isulong) {
      return TypeKind.TP_ULONG;
    }

    // long long
    // signed long long
    // long long int
    // signed long long int
    boolean islonglong = istype(flag, f_longlong, f_signed | f_int);
    if (islonglong) {
      return TypeKind.TP_LONG_LONG;
    }

    // unsigned long long
    // unsigned long long int
    boolean isulonglong = istype(flag, f_longlong | f_unsigned, f_int);
    if (isulonglong) {
      return TypeKind.TP_ULONG_LONG;
    }

    // float
    boolean isfloat = istype(flag, f_float, 0);
    if (isfloat) {
      return TypeKind.TP_FLOAT;
    }

    // double
    boolean isdouble = istype(flag, f_double, 0);
    if (isdouble) {
      return TypeKind.TP_DOUBLE;
    }

    // long double
    boolean isldouble = istype(flag, f_long | f_double, 0);
    if (isldouble) {
      return TypeKind.TP_LONG_DOUBLE;
    }

    throw new ParseException(lastLoc + "error:" + "unknown type-specifier [" + ts.toString() + "]");
  }

  private static void checkdup(int cnt, int max, String loc, String msg) {
    if (cnt >= max) {
      throw new ParseException(loc + "error:" + "duplicate " + msg);
    }
  }

  private static void checkzero(int cnt, String loc, String msg) {
    if (cnt != 0) {
      throw new ParseException(loc + "error:" + msg);
    }
  }

  private static boolean istype(int f, int fmask, int fclear) {
    int flag = f;
    flag &= ~fclear;

    if ((flag & fmask) == fmask) {
      int zero = flag ^ (flag & fmask);
      return (zero == 0);
    }
    return false;
  }

}
