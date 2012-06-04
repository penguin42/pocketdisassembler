// (c) 2012 Dr. David Alan Gilbert <dave@treblig.org>

package org.treblig.pocketdisassembler;

import org.treblig.pocketdisassembler.SubarchOption;

// The information for one subarch entry for an architecture
public class SubarchDef {
  public SubarchDef(String _nameDisplayed, String _nameToDiss, SubarchOption[] _opts) {
	nameDisplayed = _nameDisplayed;
	nameToDiss = _nameToDiss;
	opts = _opts;
  }
  
  public String nameDisplayed;  // The displayed name
  public String nameToDiss; // The name passed to the disassembler
  public SubarchOption[] opts; // Options selectable for this subarch - can be null for no option
}
