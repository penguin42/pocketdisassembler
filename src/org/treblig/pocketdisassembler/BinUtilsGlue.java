// (c) 2011-2012 Dr. David Alan Gilbert <dave@treblig.org>
//    This source is released under the GNU Public License V3 or later
//    a copy of which you should have received with this source.
package org.treblig.pocketdisassembler;

public class BinUtilsGlue {
  static {
	  System.loadLibrary("bfd-2.22");
	  System.loadLibrary("opcodes-2.22");
	  System.loadLibrary("binutilsglue");
  }
  
  public native String doDisassemble(String arch, String subarch, String options, int flags, byte[] rawdata);
}
