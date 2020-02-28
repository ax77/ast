package ast.stmt.parser;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.TOKEN_IDENT;
import static jscan.tokenize.T.T_COLON;
import static jscan.tokenize.T.T_LEFT_PAREN;
import static jscan.tokenize.T.T_RIGHT_PAREN;
import static jscan.tokenize.T.T_SEMI_COLON;

import java.util.ArrayList;
import java.util.List;

import ast.declarations.Declaration;
import ast.declarations.parser.ParseDeclarations;
import ast.expr.CExpression;
import ast.expr.parser.ParseExpression;
import ast.parse.Parse;
import ast.stmt.Scase;
import ast.stmt.Sdefault;
import ast.stmt.Sswitch;
import ast.stmt.main.CStatement;
import ast.stmt.main.CStatementBase;
import ast.stmt.sem.BreakContinueStrayCheck;
import ast.unit.BlockItem;
import ast.unit.FunctionDefinition;
import jscan.hashed.Hash_ident;
import jscan.symtab.Ident;
import jscan.tokenize.T;
import jscan.tokenize.Token;

public class ParseStatement {
  private final Parse parser;

  public ParseStatement(Parse parser) {
    this.parser = parser;
  }

  private CExpression e_expression() {
    return new ParseExpression(parser).e_expression();
  }

  public CStatement parse_coumpound_stmt(boolean isFunctionStart) {
    if (!isFunctionStart) {
      parser.pushscope();
    }

    List<BlockItem> blocks = new ArrayList<BlockItem>(0);

    Token lbrace = parser.checkedMove(T.T_LEFT_BRACE);

    if (parser.tp() == T.T_RIGHT_BRACE) {
      Token rbrace = parser.checkedMove(T.T_RIGHT_BRACE);

      if (!isFunctionStart) {
        parser.popscope(); // XXX:
      }

      return new CStatement(lbrace, rbrace, blocks);
    }

    BlockItem block = parse_one_block();
    for (;;) {
      blocks.add(block);
      if (parser.tp() == T.T_RIGHT_BRACE) {
        break;
      }
      if (block == null) {
        break;
      }
      block = parse_one_block();
    }

    Token rbrace = parser.checkedMove(T.T_RIGHT_BRACE);

    if (!isFunctionStart) {
      parser.popscope();
    }

    return new CStatement(lbrace, rbrace, blocks);
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
      return parse_coumpound_stmt(false);
    }

    // expression-statement by default
    //
    Token from = parser.tok();
    CExpression expr = e_expression();
    CStatement ret = new CStatement(from, CStatementBase.SEXPR, expr);
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

    parser.checkedMove(T.T_RIGHT_PAREN);
    parser.checkedMove(T_SEMI_COLON);

    return new CStatement(startLoc, asmlist);
  }

  private BlockItem parse_one_block() {

    // TODO: doe's it correct, or maybe clean way exists?
    // different scope between names of labels and all other names.
    // why ??? 
    if (parser.tok().ofType(TOKEN_IDENT) || !parser.tok().isBuiltinIdent()) {
      Token peek = parser.getTokenlist().peek();
      if (peek.ofType(T_COLON)) {
        BlockItem block = new BlockItem();
        block.setStatement(parse_label());
        return block;
      }
    }

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

    Token from = parser.checkedMove(Hash_ident.default_ident);
    parser.checkedMove(T_COLON);

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

    FunctionDefinition function = null;
    Ident label = null;
    CStatement labelstmt = null;

    if (parser.getCurrentFn() == null) {
      parser.perror("label statement outside function");
    }
    function = parser.getCurrentFn();

    Token from = parser.expectIdentifier();
    label = from.getIdent();
    parser.checkedMove(T_COLON);

    labelstmt = parse_statement();

    return new CStatement(from, CStatementBase.SLABEL, function, label, labelstmt);
  }

  private CStatement parse_goto() {

    FunctionDefinition function = null;
    Ident label = null;
    CStatement labelstmt = null;

    if (parser.getCurrentFn() == null) {
      parser.perror("goto statement outside function");
    }
    function = parser.getCurrentFn();

    Token from = parser.checkedMove(Hash_ident.goto_ident);

    Token identTok = parser.expectIdentifier();
    label = identTok.getIdent();

    parser.semicolon();
    return new CStatement(from, CStatementBase.SGOTO, function, label, labelstmt);
  }

  private CStatement parse_return() {
    Token from = parser.checkedMove(Hash_ident.return_ident);

    if (parser.tp() == T_SEMI_COLON) {
      parser.move();
      return new CStatement(from, CStatementBase.SRETURN, null);
    }

    CExpression retexpr = e_expression();

    parser.checkedMove(T_SEMI_COLON);
    return new CStatement(from, CStatementBase.SRETURN, retexpr);
  }

  private CStatement parse_dowhile() {
    CExpression test = null;
    CStatement loop = null;

    parser.pushLoop("do_while");

    Token from = parser.checkedMove(Hash_ident.do_ident);

    loop = parse_statement();
    parser.checkedMove(Hash_ident.while_ident);

    test = new ParseExpression(parser).getExprInParen();
    parser.semicolon();

    parser.popLoop();
    return new CStatement(from, CStatementBase.SDOWHILE, test, loop);
  }

  private CStatement parse_for() {

    Declaration decl = null;
    CExpression init = null;
    CExpression test = null;
    CExpression step = null;
    CStatement loop = null;

    parser.pushLoop("for");
    parser.pushscope(); // TODO:

    Token from = parser.checkedMove(Hash_ident.for_ident);
    parser.lparen();

    if (parser.tp() != T_SEMI_COLON) {

      if (parser.isDeclSpecStart()) {
        decl = new ParseDeclarations(parser).parseDeclaration(); // XXX: semicolon moved in parse_declaration()
      }

      else {
        init = e_expression();
        parser.semicolon();
      }
    }

    else {
      parser.semicolon();
    }

    if (parser.tp() != T_SEMI_COLON) {
      test = e_expression();
    }
    parser.semicolon();

    if (parser.tp() != T_RIGHT_PAREN) {
      step = e_expression();
    }
    parser.rparen();

    loop = parse_statement();

    parser.popLoop();
    parser.popscope(); // TODO:
    return new CStatement(from, decl, init, test, step, loop);
  }

  private CStatement parse_switch() {

    Token from = parser.checkedMove(Hash_ident.switch_ident);
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

    Token from = parser.checkedMove(Hash_ident.case_ident);
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
    CExpression ifexpr = null;
    CStatement ifstmt = null;
    CStatement ifelse = null;

    Token from = parser.checkedMove(Hash_ident.if_ident);

    ifexpr = new ParseExpression(parser).getExprInParen();
    ifstmt = parse_statement();

    if (parser.tok().isIdent(Hash_ident.else_ident)) {
      Token elsekw = parser.checkedMove(Hash_ident.else_ident);
      ifelse = parse_statement();

      return new CStatement(elsekw, ifexpr, ifstmt, ifelse);
    }

    return new CStatement(from, ifexpr, ifstmt, ifelse);
  }

  private CStatement parse_while() {
    CExpression test = null;
    CStatement loop = null;

    parser.pushLoop("while");

    Token from = parser.checkedMove(Hash_ident.while_ident);
    test = new ParseExpression(parser).getExprInParen();
    loop = parse_statement();

    parser.popLoop();
    return new CStatement(from, CStatementBase.SWHILE, test, loop);
  }

}
