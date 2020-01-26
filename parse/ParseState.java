package ast.parse;

import java.util.List;
import java.util.Stack;

import ast.stmt.Sswitch;
import ast.symtabg.Symtab;
import ast.symtabg.elements.CSymbol;
import ast.unit.FunctionDefinition;
import jscan.symtab.Ident;
import jscan.tokenize.Token;

public class ParseState {
  private final int tokenlistOffset;
  private final Token tok;
  private final FunctionDefinition currentFn;
  private final List<Token> ringBuffer;
  private final String lastloc;

  private final Symtab<Ident, CSymbol> symbols;
  private final Symtab<Ident, CSymbol> tags;
  private final Stack<Sswitch> switches;
  private final Stack<String> loops;

  public ParseState(Parse parser) {
    this.tokenlistOffset = parser.getTokenlist().getOffset();
    this.tok = parser.tok();
    this.currentFn = parser.getCurrentFn();
    this.ringBuffer = parser.getRingBuffer();
    this.lastloc = parser.getLastLoc();
    this.symbols = parser.getSymbols();
    this.tags = parser.getTags();
    this.switches = parser.getSwitches();
    this.loops = parser.getLoops();
  }

  public int getTokenlistOffset() {
    return tokenlistOffset;
  }

  public Token getTok() {
    return tok;
  }

  public FunctionDefinition getCurrentFn() {
    return currentFn;
  }

  public List<Token> getRingBuffer() {
    return ringBuffer;
  }

  public String getLastloc() {
    return lastloc;
  }

  public Symtab<Ident, CSymbol> getSymbols() {
    return symbols;
  }

  public Symtab<Ident, CSymbol> getTags() {
    return tags;
  }

  public Stack<Sswitch> getSwitches() {
    return switches;
  }

  public Stack<String> getLoops() {
    return loops;
  }

}
