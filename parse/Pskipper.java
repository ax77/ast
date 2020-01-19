package ast.parse;

import static jscan.tokenize.T.TOKEN_EOF;
import static jscan.tokenize.T.T_LEFT_PAREN;
import static jscan.tokenize.T.T_RIGHT_PAREN;

import java.util.ArrayList;
import java.util.List;

import jscan.tokenize.Token;

public class Pskipper {
  private final Parse p;

  public Pskipper(Parse p) {
    this.p = p;
  }

  public boolean skipAsm() {
    if (!p.isAsmStart()) {
      return false;
    }

    List<Token> attrlist = new ArrayList<Token>();
    int nest = 0;
    String startLoc = p.getLastLoc();

    attrlist.add(p.tok()); // __asm
    p.move();

    while (p.isTypeQual()) {
      Token saved = p.tok();
      p.move();
      attrlist.add(saved);
    }

    if (!p.tok().ofType(T_LEFT_PAREN)) {
      p.perror("expect `(` after __asm");
    }

    // 
    Token lparen = p.tok();
    p.move();
    attrlist.add(lparen);

    while (!p.isEof()) {

      if (p.tp() == TOKEN_EOF) {
        p.perror("unclosed attribute list started at: " + startLoc);
      } else if (p.tp() == T_LEFT_PAREN) {
        nest++;
      } else if (p.tp() == T_RIGHT_PAREN) {
        if (--nest < 0) {
          Token saved = p.tok();
          p.move();
          attrlist.add(saved);
          break;
        }
      } else {
        Token saved = p.tok();
        p.move();
        attrlist.add(saved);
      }

    }

    return true;
  }

  public boolean skipOneAttribute() {

    if (!p.isAttributeStart()) {
      return false;
    }

    List<Token> attrlist = new ArrayList<Token>();
    int nest = 0;
    String startLoc = p.getLastLoc();

    while (!p.isEof()) {
      attrlist.add(p.tok());
      p.move();
      if (p.tp() == TOKEN_EOF) {
        p.perror("unclosed attribute list started at: " + startLoc);
      }
      if (p.tp() == T_LEFT_PAREN) {
        nest++;
      }
      if (p.tp() == T_RIGHT_PAREN) {
        if (--nest == 0) {
          attrlist.add(p.tok());
          break;
        }
      }
    }
    return true;
  }

  public boolean skipAttributes() {
    boolean f = false;
    while (skipOneAttribute()) {
      f = true;
      p.move();
    }
    return f;
  }

  public boolean skipAttributesAndAsm() {
    boolean atrs = false;
    boolean asms = false;
    if (skipAttributes()) {
      atrs = true;
    }
    if (skipAsm()) {
      asms = true;
    }
    return atrs || asms;
  }

}
