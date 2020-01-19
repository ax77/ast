package ast.stmt;

import java.util.List;

import jscan.tokenize.Token;

public class Sasm {
  private List<Token> asmstmt;

  public Sasm(List<Token> asmstmt) {
    super();
    this.asmstmt = asmstmt;
  }

  public List<Token> getAsmstmt() {
    return asmstmt;
  }

  public void setAsmstmt(List<Token> asmstmt) {
    this.asmstmt = asmstmt;
  }

}
