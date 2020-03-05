package ast.decls.parser;

import jscan.hashed.Hash_ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.expr.CExpression;
import ast.expr.parser.ParseExpression;
import ast.expr.sem.ConstexprEval;
import ast.parse.Parse;

public class ParseStaticAssert {
  private final Parse parser;

  public ParseStaticAssert(Parse parser) {
    this.parser = parser;
  }

  public boolean isStaticAssertAndItsOk() {

    //  static_assert_declaration
    //    : STATIC_ASSERT '(' constant_expression ',' STRING_LITERAL ')' ';'
    //    ;

    if (!parser.tok().isIdent(Hash_ident._Static_assert_ident)) {
      return false;
    }

    parser.checkedMove(Hash_ident._Static_assert_ident);
    parser.lparen();

    CExpression ce = new ParseExpression(parser).e_const_expr();
    parser.checkedMove(T.T_COMMA);

    Token message = parser.checkedMove(T.TOKEN_STRING);
    parser.rparen();
    parser.semicolon();

    long sares = new ConstexprEval(parser).ce(ce);
    if (sares == 0) {
      parser.perror("static-assert fail with message: " + message.getValue());
    }

    return true;
  }

}
