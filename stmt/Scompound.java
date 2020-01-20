package ast.stmt;

import java.util.ArrayList;
import java.util.List;

import jscan.sourceloc.SourceLocation;
import jscan.sourceloc.SourceLocationRange;
import jscan.tokenize.Token;
import ast.unit.BlockItem;

public class Scompound {
  private final List<BlockItem> blockItemList;

  private SourceLocationRange bracesPos;

  public Scompound() {
    this.blockItemList = new ArrayList<BlockItem>(0);
  }

  public void push(BlockItem e) {
    blockItemList.add(e);
  }

  public List<BlockItem> getBlockItemList() {
    return blockItemList;
  }

  public void setPos(Token lbrace, Token rbrace) {
    this.bracesPos = new SourceLocationRange(lbrace, rbrace);
  }

  public SourceLocationRange getBracesPos() {
    return bracesPos;
  }

  public void setBracesPos(SourceLocationRange bracesPos) {
    this.bracesPos = bracesPos;
  }

  public SourceLocation getBeginPos() {
    return bracesPos.getBeginPos();
  }

  public SourceLocation getEndPos() {
    return bracesPos.getEndPos();
  }

}
