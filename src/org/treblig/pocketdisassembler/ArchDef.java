//(c) 2012 Dr. David Alan Gilbert <dave@treblig.org>

// The information for one arch entry
package org.treblig.pocketdisassembler;

import org.treblig.pocketdisassembler.SubarchDef;

public class ArchDef {
  public ArchDef(String _nameDisplayed, String _nameToDiss, SubarchDef[] _subDefs) {
		nameDisplayed = _nameDisplayed;
		nameToDiss = _nameToDiss;
		subDefs = _subDefs;
  }

  public String nameDisplayed;  // The displayed name
  public String nameToDiss; // The name passed to the disassembler
  public SubarchDef[] subDefs; // The subarch definitions
}
