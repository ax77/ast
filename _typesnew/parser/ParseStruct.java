package ast._typesnew.parser;

import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_COLON;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import ast._typesnew.CStructField;
import ast._typesnew.CStructType;
import ast._typesnew.CType;
import ast._typesnew.decl.CDecl;
import ast._typesnew.sem.SemanticBitfield;
import ast._typesnew.util.TypeMerger;
import ast.declarations.parser.ParseDeclarations;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.Parse;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseStruct {
  private final Parse parser;

  public ParseStruct(Parse parser) {
    this.parser = parser;
  }

  public CStructType parseStruct(boolean isUnion) {
    //struct ...
    //       ^

    boolean iscorrect = parser.tok().ofType(TOKEN_IDENT) || parser.tok().ofType(T.T_LEFT_BRACE);
    if (!iscorrect) {
      parser.perror("expect identifier or { for enum type-specifier");
    }

    Token tag = null;
    if (parser.tok().ofType(TOKEN_IDENT)) {
      tag = parser.tok();
      parser.move();
    }

    // ref
    if (parser.tp() != T.T_LEFT_BRACE) {

      TypeMerger.checkTagNotNullForReference(tag);
      CStructType strref = new CStructType(isUnion, tag.getIdent());
      return strref;
    }

    // def
    List<CStructField> fields = parseFields(parser);
    CStructType strdef = new CStructType(isUnion, TypeMerger.getIdentOrNull(tag), fields);
    return strdef;
  }

  //////////////////////////////////////////////
  //struct_declaration_list
  //  : struct_declaration
  //  | struct_declaration_list struct_declaration
  //  ;
  //
  //struct_declaration
  //  : specifier_qualifier_list ';'  /* for anonymous struct/union */
  //  | specifier_qualifier_list struct_declarator_list ';'
  //  | static_assert_declaration
  //  ;

  private List<CStructField> parseFields(Parse parser) {
    parser.checkedMove(T.T_LEFT_BRACE);
    List<CStructField> structDeclarationList = new ArrayList<CStructField>(0);

    // struct S {};
    //           ^
    if (parser.tp() == T.T_RIGHT_BRACE) {
      parser.pwarning("empty struct declaration list");

      parser.checkedMove(T.T_RIGHT_BRACE); // TODO:location
      return structDeclarationList;
    }

    List<CStructField> structDeclaration = parseStructDeclaration();
    structDeclarationList.addAll(structDeclaration);

    while (parser.isDeclSpecStart()) {
      List<CStructField> structDeclarationSeq = parseStructDeclaration();
      structDeclarationList.addAll(structDeclarationSeq);
    }

    parser.checkedMove(T.T_RIGHT_BRACE);
    return structDeclarationList;
  }

  //struct_declaration
  //  : specifier_qualifier_list ';'  /* for anonymous struct/union */
  //  | specifier_qualifier_list struct_declarator_list ';'
  //  | static_assert_declaration
  //  ;

  private List<CStructField> parseStructDeclaration() {

    List<CStructField> r = new ArrayList<CStructField>(0);

    // static_assert
    //
    boolean skip = new ParseDeclarations(parser).isStaticAssertAndItsOk();
    if (skip) {
      return r;
    }

    // TODO: this is spec-qual
    // no storage here...
    CType basetype = new ParseBase(parser).parseBase();

    if (parser.tp() == T_SEMI_COLON) {
      parser.move();

      // TODO:XXX
      // this mean:
      // 1) we inside struct/union
      // 2) we get something in declspecs
      // 3) we find semicolon
      // 4) this field has no name
      // this may be struct, union, enum
      // this may have tag or not
      // if this has a tag OR name is not anonymous
      // if this has a tag, and no name - is warning 'declaration doe's not declare anything'

      boolean isStructUnionEnum = basetype.isStrUnion() || basetype.isEnumeration();
      if (!isStructUnionEnum) {
        parser.perror("expect struct/union");
      }

      // TODO:XXX: fields offset, if it's from anonymous UNION...

      if (basetype.isEnumeration()) {
        //TODO:
        return r;
      } else {

        boolean isAnonymousDeclaration = !basetype.getTpStruct().isHasTag();
        if (isAnonymousDeclaration) {

          List<CStructField> fieldsInside = basetype.getTpStruct().getFields();
          r.addAll(fieldsInside);
          return r;
        } else {
          parser.pwarning("declaration doe's not declare anything.");
          return r; // TODO: empty now ?
        }

      }

    }

    // otherwise declarator-list like: [int a,b,c;]
    parseStructDeclaratorList(r, basetype);
    parser.checkedMove(T_SEMI_COLON);

    return r;
  }

  //struct_declarator_list
  //  : struct_declarator
  //  | struct_declarator_list ',' struct_declarator
  //  ;
  //
  //struct_declarator
  //  : ':' constant_expression
  //  | declarator ':' constant_expression
  //  | declarator
  //  ;

  private void parseStructDeclaratorList(List<CStructField> out, CType specqual) {

    CStructField structDeclarator = parseStructDeclarator(parser, specqual);
    out.add(structDeclarator);

    while (parser.tp() == T.T_COMMA) {
      parser.move();
      CStructField structDeclaratorSeq = parseStructDeclarator(parser, specqual);
      out.add(structDeclaratorSeq);
    }

  }

  //struct_declarator
  //  : ':' constant_expression
  //  | declarator ':' constant_expression
  //  | declarator
  //  ;

  private CStructField parseStructDeclarator(Parse parser, CType base) {

    // unnamed-bit-field
    //
    if (parser.tp() == T_COLON) {
      parser.move();

      CExpression consterpr = new ParseExpression(parser).e_const_expr();
      int width = (int) new ConstexprEval(parser).ce(consterpr);

      final CType bf = new SemanticBitfield(parser).buildBitfield(base, width);
      final CStructField unnamedBitfield = new CStructField(bf);
      return unnamedBitfield;
    }

    // need normal field or named bit-field

    CDecl decl = new ParseDecl(parser).parseDecl();
    CType type = TypeMerger.build(base, decl);

    // named-bit-field
    //
    if (parser.tp() == T_COLON) {
      parser.move();

      CExpression consterpr = new ParseExpression(parser).e_const_expr();
      int width = (int) new ConstexprEval(parser).ce(consterpr);

      final CType bf = new SemanticBitfield(parser).buildBitfield(type, width);
      final CStructField namedBitfield = new CStructField(decl.getName(), bf);
      return namedBitfield;
    }

    // plain-field
    //
    final CStructField plainField = new CStructField(decl.getName(), type);
    return plainField;
  }
}