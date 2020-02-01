package ast.initarr;

import java.util.ArrayList;
import java.util.List;

import jscan.preprocess.ScanExc;
import ast.declarations.Initializer;
import ast.declarations.InitializerList;
import ast.declarations.InitializerListEntry;
import ast.errors.ParseException;
import ast.symtabg.elements.CSymbol;

public abstract class BlocksBuilder {

  private static int level = -1;
  private static List<OffsetInitializerEntry> entries = new ArrayList<OffsetInitializerEntry>(0);

  public static Blocks build(CSymbol sym, Initializer initializer) {

    level = -1;
    entries = new ArrayList<OffsetInitializerEntry>(0);

    Blocks blocks = new Blocks(sym);
    buildIndices(initializer, blocks);
    blocks.merge();

    return blocks;

  }

  private static void err(String m) {
    throw new ParseException(m);
  }

  private static void buildIndices(Initializer initializer, Blocks blocks) {

    if (level > blocks.getMdeep()) {
      err("too many braces.");
    }

    if (!initializer.isHasInitializerList()) {
      final OffsetInitializerEntry ent = new OffsetInitializerEntry(level, initializer.getAssignment());
      entries.add(ent);

      if (level == 0) {
        if (entries.size() > 1) {
          err("size.....");
        }

        blocks.pushToWild(entries);
        entries = new ArrayList<OffsetInitializerEntry>(0);
      }
    }

    else {

      ++level;
      final InitializerList initializerList = initializer.getInitializerList();
      final List<InitializerListEntry> initializers = initializerList.getInitializers();
      for (int j = 0; j < initializers.size(); j++) {
        InitializerListEntry entry = initializers.get(j);
        if (entry.isHasDesignatorsBefore()) {
          throw new ScanExc("unsupported now");
        }
        buildIndices(entry.getInitializer(), blocks);
      }
      --level;

      if (level == 0) {
        blocks.pushToBlocks(entries);
        entries = new ArrayList<OffsetInitializerEntry>(0);
      }
    }

  }
}
