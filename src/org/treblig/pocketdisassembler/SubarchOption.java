// (c) 2012 Dr. David Alan Gilbert <dave@treblig.org>
package org.treblig.pocketdisassembler;

// One option that may be displayed or maybe fixed.
public class SubarchOption {
	// Flags that an option can have
	public static final int FlagBigEndian = 1;
	
    public SubarchOption(String _name, boolean _trueByDefault, boolean _visible, String _trueText, String _falseText, int _trueFlags) {
  	  name=_name;
  	  trueByDefault=_trueByDefault;
  	  visible=_visible;
  	  trueText=_trueText;
  	  falseText=_falseText;
  	  trueFlags=_trueFlags;
    }
    public String name;
    public boolean trueByDefault;
    public boolean visible;
    public String trueText;
    public String falseText;
    public int trueFlags;
  }
