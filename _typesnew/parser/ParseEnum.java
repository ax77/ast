package ast._typesnew.parser;

import static jscan.tokenize.T.TOKEN_IDENT;

import java.util.ArrayList;
import java.util.List;

import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast._typesnew.CEnumType;
import ast._typesnew.CType;
import ast._typesnew.main.StorageKind;
import ast._typesnew.main.TypeKind;
import ast._typesnew.util.TypeMerger;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.Parse;
import ast.parse.ParseException;
import ast.symtabg.elements.CSymbol;

class EnumDto {
  private final Parse parser;
  private int minvalue;
  private int maxvalue;
  private List<Ident> enumerators;
  private int curvalue;

  public EnumDto(Parse parser) {
    this.parser = parser;
    this.enumerators = new ArrayList<Ident>(0);
  }

  public void addEnumerator(Ident id, int curvalue) {
    if (enumerators.contains(id)) {
      parser.perror("duplicate enum value: " + id.getName()); // TODO: ambiguous with symDef()
    }
    this.enumerators.add(id);
    this.curvalue = curvalue;

    this.minvalue = Math.min(this.minvalue, this.curvalue);
    this.maxvalue = Math.max(this.maxvalue, this.curvalue);
    this.curvalue += 1;
  }

  public int getCurvalue() {
    return curvalue;
  }
}

public class ParseEnum {
  private final Parse parser;

  public ParseEnum(Parse parser) {
    this.parser = parser;
  }

  public CEnumType parseEnum() {
    //enum ...
    //     ^

    boolean iscorrect = parser.tok().ofType(TOKEN_IDENT) || parser.tok().ofType(T.T_LEFT_BRACE);
    if (!iscorrect) {
      parser.perror("expect identifier or { for enum type-specifier");
    }

    Token tag = null;
    if (parser.tok().ofType(TOKEN_IDENT)) {
      tag = parser.tok();
      parser.move();
    }

    if (parser.tp() != T.T_LEFT_BRACE) {

      checkTagNotNullForReference(tag);
      final CEnumType enumref = new CEnumType(tag.getIdent(), true);
      return enumref;
    }

    // TODO: dto semantic, enum size and align
    // depends on max value by it value
    EnumDto dto = parseEnumeratorList();

    final CEnumType enumdef = new CEnumType(TypeMerger.getIdentOrNull(tag), false);
    return enumdef;
  }

  private void checkTagNotNullForReference(Token tag) {
    if (tag == null) {
      throw new ParseException("for struct/union/enum reference tag must be present always");
    }
  }

  private EnumDto parseEnumeratorList() {

    parser.checkedMove(T.T_LEFT_BRACE);

    EnumDto enumdto = new EnumDto(parser);
    parseEnumerator(enumdto);

    while (parser.tp() == T.T_COMMA) {
      parser.move();

      //
      if (parser.tp() == T.T_RIGHT_BRACE) {
        // taint comma: 
        // enum { a,b,c, }
        //             ^
        break;
      }
      //

      parseEnumerator(enumdto);
    }

    parser.checkedMove(T.T_RIGHT_BRACE);
    return enumdto;
  }

  private void parseEnumerator(EnumDto enumdto) {
    Token tok = parser.expectIdentifier();
    Ident identifier = tok.getIdent();

    int enumvalue = enumdto.getCurvalue();

    if (parser.tp() != T.T_ASSIGN) {
      final CSymbol symbol = new CSymbol(identifier, new CType(TypeKind.TP_ENUM, StorageKind.ST_STATIC), tok); // TODO:Storage
      symbol.setEnumvalue(enumvalue);

      parser.defineSym(identifier, symbol);
      enumdto.addEnumerator(identifier, enumvalue);

      return; // XXX:
    }

    parser.checkedMove(T.T_ASSIGN);

    CExpression constexpr = new ParseExpression(parser).e_const_expr();
    enumvalue = (int) new ConstexprEval(parser).ce(constexpr);

    final CSymbol symbol = new CSymbol(identifier, new CType(TypeKind.TP_ENUM, StorageKind.ST_STATIC), tok); // TODO:Storage
    symbol.setEnumvalue(enumvalue);

    parser.defineSym(identifier, symbol);
    enumdto.addEnumerator(identifier, enumvalue);

  }
}
