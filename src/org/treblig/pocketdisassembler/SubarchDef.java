// (c) 2012 Dr. David Alan Gilbert <dave@treblig.org>

package org.treblig.pocketdisassembler;

// The information for one subarch entry for an architecture
public class SubarchDef {
  public SubarchDef(String _nameDisplayed, String _nameToDiss) {
	nameDisplayed = _nameDisplayed;
	nameToDiss = _nameToDiss;
  }
  
  public String nameDisplayed;  // The displayed name
  public String nameToDiss; // The name passed to the disassembler
}
