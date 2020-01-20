package ast.stmt.parser;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_COLON;
import static jscan.tokenize.T.T_LEFT_PAREN;
import static jscan.tokenize.T.T_RIGHT_PAREN;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import jscan.hashed.Hash_ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;
import ast.declarations.main.Declaration;
import ast.declarations.parser.ParseDeclarations;
import ast.expr.main.CExpression;
import ast.expr.parser.ParseExpression;
import ast.parse.Parse;
import ast.stmt.Sasm;
import ast.stmt.Scase;
import ast.stmt.Scompound;
import ast.stmt.Sdefault;
import ast.stmt.Sdowhile;
import ast.stmt.Sexpr;
import ast.stmt.Sfor;
import ast.stmt.Sgoto;
import ast.stmt.Sif;
import ast.stmt.Slabel;
import ast.stmt.Sreturn;
import ast.stmt.Sswitch;
import ast.stmt.Swhile;
import ast.stmt.main.CStatement;
import ast.stmt.main.CStatementBase;
import ast.stmt.sem.BreakContinueStrayCheck;
import ast.unit.BlockItem;

public class ParseStatement {
  private final Parse parser;

  public ParseStatement(Parse parser) {
    this.parser = parser;
  }

  private CExpression e_expression() {
    return new ParseExpression(parser).e_expression();
  }

  public Scompound parse_coumpound_stmt() {
    parser.pushscope();

    Scompound compoundStatement = new Scompound();

    Token lbrace = parser.checkedGetT(T.T_LEFT_BRACE);

    if (parser.tp() == T.T_RIGHT_BRACE) {
      Token rbrace = parser.checkedGetT(T.T_RIGHT_BRACE);

      parser.popscope(); // XXX:
      compoundStatement.setPos(lbrace, rbrace);
      return compoundStatement;
    }

    BlockItem block = parse_one_block();
    for (;;) {
      compoundStatement.push(block);
      if (parser.tp() == T.T_RIGHT_BRACE) {
        break;
      }
      if (block == null) {
        break;
      }
      block = parse_one_block();
    }

    Token rbrace = parser.checkedGetT(T.T_RIGHT_BRACE);
    compoundStatement.setPos(lbrace, rbrace);

    parser.popscope();
    return compoundStatement;
  }

  public CStatement parse_statement() {
    // ;
    // {
    // for (
    // while (
    // do {
    // if (
    // return
    // goto ident
    // ident :
    // switch (
    // expression ;
    // 

    if (parser.tok().ofType(T_SEMI_COLON)) {
      Token from = parser.semicolon();
      return new CStatement(from, CStatementBase.SSEMICOLON);
    }

    if (parser.tok().isIdent(Hash_ident.default_ident)) {
      return parse_default();
    }

    if (parser.tp() == TOKEN_IDENT) {
      Token peek = parser.getTokenlist().peek();
      if (peek.ofType(T_COLON)) {
        return parse_label();
      }
    }

    if (parser.isAsmStart()) {
      return parse_asm();
    }

    if (parser.tok().isIdent(Hash_ident.goto_ident)) {
      return parse_goto();
    }

    if (parser.tok().isIdent(Hash_ident.return_ident)) {
      return parse_return();
    }

    if (parser.tok().isIdent(Hash_ident.for_ident)) {
      return parse_for();
    }

    if (parser.tok().isIdent(Hash_ident.switch_ident)) {
      return parse_switch();
    }

    if (parser.tok().isIdent(Hash_ident.case_ident)) {
      return parse_case();
    }

    if (parser.tok().isIdent(Hash_ident.if_ident)) {
      return parse_if();
    }

    if (parser.tok().isIdent(Hash_ident.while_ident)) {
      return parse_while();
    }

    if (parser.tok().isIdent(Hash_ident.do_ident)) {
      return parse_dowhile();
    }

    if (parser.tok().isIdent(Hash_ident.break_ident)) {
      return new BreakContinueStrayCheck(parser).breakStatement();
    }

    if (parser.tok().isIdent(Hash_ident.continue_ident)) {
      return new BreakContinueStrayCheck(parser).continueStatement();
    }

    if (parser.tok().ofType(T.T_LEFT_BRACE)) {
      return new CStatement(parse_coumpound_stmt());
    }

    // expression-statement by default
    //
    Token from = parser.tok();
    CExpression expr = e_expression();
    Sexpr es = new Sexpr(expr);
    CStatement ret = new CStatement(from, es);
    parser.semicolon();
    return ret;
  }

  private CStatement parse_asm() {

    List<Token> asmlist = new ArrayList<Token>();
    int nest = 0;
    Token startLoc = parser.tok();

    while (!parser.isEof()) {
      asmlist.add(parser.tok());
      parser.move();
      if (parser.tp() == TOKEN_EOF) {
        parser.perror("unclosed asm statement at: " + startLoc.loc());
      }
      if (parser.tp() == T_LEFT_PAREN) {
        nest++;
      }
      if (parser.tp() == T_RIGHT_PAREN) {
        if (--nest == 0) {
          asmlist.add(parser.tok());
          break;
        }
      }
    }

    parser.checkedGetT(T.T_RIGHT_PAREN);
    parser.checkedGetT(T_SEMI_COLON);

    Sasm asmstmt_ = new Sasm(asmlist);
    return new CStatement(startLoc, asmstmt_);
  }

  private BlockItem parse_one_block() {

    if (parser.isDeclSpecStart()) {
      Declaration dec = new ParseDeclarations(parser).parseDeclaration();

      BlockItem block = new BlockItem();
      block.setDeclaration(dec);
      return block;
    }

    CStatement stmt = parse_statement();
    if (stmt != null) {
      BlockItem block = new BlockItem();
      block.setStatement(stmt);
      return block;
    }

    return null;
  }

  private CStatement parse_default() {

    Token from = parser.checkedMoveIdent(Hash_ident.default_ident);
    parser.checkedGetT(T_COLON);

    if (parser.tp() == T.T_RIGHT_BRACE) {
      parser.perror("default label without statement");
    }

    if (parser.getSwitches().isEmpty()) {
      parser.perror("default label outside switch");
    }

    CStatement defstmt = parse_statement();
    Sswitch parent = parser.peekSwitch();

    Sdefault retstmt = new Sdefault(parent, defstmt);
    parent.setDefault_stmt(retstmt);

    return new CStatement(from, retstmt);
  }

  private CStatement parse_label() {

    if (parser.getCurrentFn() == null) {
      parser.perror("label statement outside function");
    }

    Token ident = parser.expectIdentifier();
    parser.checkedGetT(T_COLON);

    CStatement labelstmt = parse_statement();
    Slabel label = new Slabel(parser.getCurrentFn(), ident.getIdent(), labelstmt);

    return new CStatement(ident, label);
  }

  private CStatement parse_goto() {

    if (parser.getCurrentFn() == null) {
      parser.perror("goto statement outside function");
    }

    Token from = parser.checkedMoveIdent(Hash_ident.goto_ident);

    Token ident = parser.expectIdentifier();
    Sgoto gotostmt = new Sgoto(parser.getCurrentFn(), ident.getIdent());

    parser.semicolon();
    return new CStatement(from, gotostmt);
  }

  private CStatement parse_return() {
    Token from = parser.checkedMoveIdent(Hash_ident.return_ident);

    if (parser.tp() == T_SEMI_COLON) {
      parser.move();
      Sreturn retstmt = new Sreturn();
      return new CStatement(from, retstmt);
    }

    CExpression retexpr = e_expression();
    Sreturn retstmt = new Sreturn(retexpr);

    parser.checkedGetT(T_SEMI_COLON);
    return new CStatement(from, retstmt);
  }

  private CStatement parse_dowhile() {
    parser.pushLoop("do_while");

    Token from = parser.checkedMoveIdent(Hash_ident.do_ident);

    CStatement loop = parse_statement();
    parser.checkedMoveIdent(Hash_ident.while_ident);

    CExpression cond = new ParseExpression(parser).getExprInParen();
    parser.semicolon();

    Sdowhile swhile = new Sdowhile(cond, loop);

    parser.popLoop();
    return new CStatement(from, CStatementBase.SDOWHILE, swhile);
  }

  private CStatement parse_for() {
    parser.pushLoop("for");
    parser.pushscope(); // TODO:

    Token from = parser.checkedMoveIdent(Hash_ident.for_ident);
    parser.lparen();

    Sfor forloop = new Sfor();

    if (parser.tp() != T_SEMI_COLON) {

      if (parser.isDeclSpecStart()) {
        Declaration decl = new ParseDeclarations(parser).parseDeclaration(); // XXX: semicolon moved in parse_declaration()
        forloop.setDecl(decl);

      }

      else {
        CExpression init = e_expression();
        forloop.setInit(init);
        parser.semicolon();
      }
    }

    else {
      parser.semicolon();
    }

    if (parser.tp() != T_SEMI_COLON) {
      CExpression test = e_expression();
      forloop.setTest(test);
    }
    parser.semicolon();

    if (parser.tp() != T_RIGHT_PAREN) {
      CExpression step = e_expression();
      forloop.setStep(step);
    }
    parser.rparen();

    CStatement loop = parse_statement();
    forloop.setLoop(loop);

    parser.popLoop();
    parser.popscope(); // TODO:
    return new CStatement(from, forloop);
  }

  private CStatement parse_switch() {

    Token from = parser.checkedMoveIdent(Hash_ident.switch_ident);
    CExpression expr = new ParseExpression(parser).getExprInParen();

    Sswitch nodeSwitch = new Sswitch(expr);
    parser.pushSwitch(nodeSwitch);

    CStatement stmt = parse_statement();
    nodeSwitch.setStmt(stmt);

    parser.popSwitch();
    return new CStatement(from, nodeSwitch);
  }

  private CStatement parse_case() {

    if (parser.getSwitches().isEmpty()) {
      parser.perror("case outside switch");
    }

    Token from = parser.checkedMoveIdent(Hash_ident.case_ident);
    CExpression expr = new ParseExpression(parser).e_const_expr();
    parser.checkedMove(T_COLON);

    Sswitch parent = parser.peekSwitch();
    Scase caselab = new Scase(parent, expr);
    parent.pushcase(caselab);

    CStatement stmt = parse_statement();
    caselab.setCasestmt(stmt);

    return new CStatement(from, caselab);
  }

  private CStatement parse_if() {
    Token from = parser.checkedMoveIdent(Hash_ident.if_ident);

    CExpression ifexpr = new ParseExpression(parser).getExprInParen();
    CStatement ifstmt = parse_statement();

    if (parser.tok().isIdent(Hash_ident.else_ident)) {
      Token elsekw = parser.checkedMoveIdent(Hash_ident.else_ident);
      CStatement elsestmt = parse_statement();

      Sif ret = new Sif(ifexpr, ifstmt, elsestmt);
      return new CStatement(elsekw, ret);
    }

    Sif ret = new Sif(ifexpr, ifstmt);
    return new CStatement(from, ret);
  }

  private CStatement parse_while() {
    parser.pushLoop("while");

    Token from = parser.checkedMoveIdent(Hash_ident.while_ident);
    CExpression cond = new ParseExpression(parser).getExprInParen();
    CStatement loop = parse_statement();

    Swhile swhile = new Swhile(cond, loop);

    parser.popLoop();
    return new CStatement(from, swhile);
  }

}
