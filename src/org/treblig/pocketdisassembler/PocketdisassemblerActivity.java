// (c) 2011-2012 Dr. David Alan Gilbert <dave@treblig.org>
//    This source is released under the GNU Public License V3 or later
//    a copy of which you should have received with this source.

package org.treblig.pocketdisassembler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.treblig.pocketdisassembler.R;
import org.treblig.pocketdisassembler.ArchDef;
import org.treblig.pocketdisassembler.SubarchOption;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class PocketdisassemblerActivity extends Activity {
	private Context mContext;
    private Button mDoDisassemble;
    private Spinner mArchSpinner, mSubArchSpinner;
    private SubArchOptViewAdapter mArchVA; // Adapter that holds the option buttons
    private BinUtilsGlue bug;
    
    private SubarchOption[] bigEndianOptSetDefTrue = {
    		new SubarchOption( "Big Endian", true, true, "", "", SubarchOption.FlagBigEndian)
    };
    private SubarchOption[] bigEndianOptSetForceTrue = {
    		new SubarchOption( "", true, false, "", "", SubarchOption.FlagBigEndian)
    };
    
    // ----------------------------------
    // Definitions for each architecture
    // ----------------------------------
    private SubarchDef[] alphaSubarchArray = {
    		new SubarchDef("alpha", "alpha", null),     new SubarchDef("ev6", "alpha:ev6", null)
    };
    // ARM option set for subarchs with visible thumb option, off by default
    private SubarchOption[] armoptThumbDefOffVisible = {
    		new SubarchOption( "thumb", false, true, "force-thumb", "no-force-thumb", 0)
    };
    // ARM option set for subarchs with visible thumb option, on by default
    private SubarchOption[] armoptThumbDefOnVisible = {
    		new SubarchOption( "thumb", true, true, "force-thumb", "no-force-thumb", 0)
    };
    
    private SubarchDef[] armSubarchArray = {
          new SubarchDef("arm", "arm", armoptThumbDefOffVisible),
          new SubarchDef("armv3m","armv3m", null), new SubarchDef("armv4","armv4", null),
          new SubarchDef("armv4t","armv4t", armoptThumbDefOnVisible),
  		  new SubarchDef("armv5te", "armv5te", armoptThumbDefOnVisible), new SubarchDef("xscale", "xscale", null),
  		  new SubarchDef("ep9312", "ep9312", armoptThumbDefOffVisible),   new SubarchDef("iwmmxt2", "iwmmxt2", armoptThumbDefOnVisible)
    };
    
    private SubarchDef[] hppaSubarchArray = {
          new SubarchDef("hppa2.0w", "hppa2.0w", null), new SubarchDef("hppa2.0", "hppa2.0", null),
		  new SubarchDef("hppa1.0",  "hppa1.0", null)
    };

    private SubarchDef[] mipsSubarchArray = {	  
		  new SubarchDef("mips", "mips", bigEndianOptSetDefTrue),       new SubarchDef("4400", "4400", bigEndianOptSetDefTrue),
		  new SubarchDef("isa32r2", "isa32r2", bigEndianOptSetDefTrue), new SubarchDef("ia6r2", "mips:ia6r2", bigEndianOptSetDefTrue), 
		  new SubarchDef("octeon", "mips:octeon", bigEndianOptSetForceTrue), new SubarchDef("loongson_3a", "mips:loongson_3a", null)
    };

    private SubarchOption[] ppc64Opts = {
    		new SubarchOption( "64",   true, false, "64",   "", 0), bigEndianOptSetForceTrue[0]
    };
    private SubarchOption[] ppc32Opts = {
    		new SubarchOption( "32",   true, false, "32",   "", 0), bigEndianOptSetForceTrue[0]
    };
    
    private SubarchDef[] ppcSubarchArray = {
		  // Note: PPC doesn't like subarch name, it prefers everything via -M and it
    	  // seems to want subarch always to be powerpc
		  new SubarchDef("ppc32", "powerpc", ppc32Opts),
		  new SubarchDef("ppc64", "powerpc", ppc64Opts)
    };
		  
    private SubarchOption[] s390zarchOpts = {
    		new SubarchOption( "zarch", true, false, "zarch", "", 0), bigEndianOptSetForceTrue[0]
    };
    private SubarchOption[] s390esaOpts = {
    		new SubarchOption( "esa",   true, false, "esa",   "", 0), bigEndianOptSetForceTrue[0]
    };
	private SubarchDef[] s390SubarchArray =	 {
		  new SubarchDef("64-bit", "s390:64-bit", s390zarchOpts), 
		  new SubarchDef("31-bit", "s390:31-bit", s390esaOpts)
    };
	
	private SubarchOption[] x86Opt32bit = {
			new SubarchOption( "i386",   true, false, "i386",   "", 0)
	};
	private SubarchOption[] x86Opt64bit = {
			new SubarchOption( "x86-64", true, false, "x86-64", "", 0)
	};
	private SubarchDef[] x86SubarchArray = {
          new SubarchDef("x86-64", "i386:x86-64", x86Opt64bit), new SubarchDef("i386", "i386", x86Opt32bit)
	};
    
    private ArchDef[] archArray = {
    	new ArchDef("alpha", "alpha", alphaSubarchArray),
    	new ArchDef("arm", "arm", armSubarchArray),
    	new ArchDef("hppa", "hppa", hppaSubarchArray),
    	new ArchDef("mips", "mips", mipsSubarchArray),
    	new ArchDef("ppc", "ppc", ppcSubarchArray),
    	new ArchDef("s390", "s390", s390SubarchArray),
    	new ArchDef("x86", "x86", x86SubarchArray)
    };
    
    private ArchDef selectedArch = archArray[0];
    private SubarchDef selectedSubarch = selectedArch.subDefs[0];
    
    // Parse a string of hex numbers, they are space separated chunks, each chunk being hex
    // digits; each chunk is decoded and appended to the result array.  Within a chunk, if the
    // chunk is an odd number of characters a 0 is prepended; then it's treated as a word of
    // chunk.length/2 bytes and added to the array in the order depending on isBigEndian
    private byte[] parseHexString(String hexstring, String[] errString, boolean isBigEndian)
    {
       ArrayList<Integer> il = new ArrayList<Integer>();
      
       // Basic input sanity
       if (!hexstring.matches("^[0-9a-f ]*$")) {
    	   errString[0]="Input must be space separated lower case hex";
    	   return new byte[0];
       }
       
       String[] chunks = hexstring.split(" ");
       
       for(int i=0;i<chunks.length;i++) {
    	 // I don't think we can get this from the split, but just to be sure
         if (chunks[i].length() == 0) continue;
         
         if (chunks[i].length() % 1 != 0)
        	 // Odd length string - prepend a 0
        	 chunks[i]="0"+chunks[i];

         int index=isBigEndian?0:chunks[i].length()-2;
         
         for(int count=0;count<chunks[i].length()/2;count++) {
           String thisByteString=chunks[i].substring(index,index+2);
           Integer thisByte=Integer.parseInt(thisByteString,16);
           il.add(thisByte);
           
           if (isBigEndian)
        	   index=index+2;
           else
        	   index=index-2;
         }
       }

       byte[] result=new byte[il.size()];
       
       
       // Surely there is an easier way....
       // ... especially given the signedness <sigh>
       for(int i=0; i<il.size(); i++) {
    	   Integer x=il.get(i);
    	   
    	   if (x < 128) {
    	     result[i]=(byte)(0+x);
    	   } else {
    		 // Turn into a signed value
    		 x=x^255;
    		 x=x+1;
    		 result[i]=(byte)(0-x);
    	   }
       }
       
       return result;
    }
    
    // --------------------------------------------------------------------------------------
    // For one of the 0-f keys - bind it to insert the text
    private void bindHexKey(int id, String key) {
    	final CharSequence toadd=key;
        ((Button)findViewById(id)).setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		EditText hexEntry = (EditText) findViewById(R.id.entryHex);
        		
        	    hexEntry.getText().insert(hexEntry.getSelectionStart(), toadd);
        	}
        });
    }
    
    // There has to be a better way - possibly can we setup the popupKeyboard extra on the entry
    // to get a custom keyboard?
    private void setupHexKeys( ) {
    	bindHexKey(R.id.hexkey0, "0");
    	bindHexKey(R.id.hexkey1, "1");
    	bindHexKey(R.id.hexkey2, "2");
    	bindHexKey(R.id.hexkey3, "3");
    	bindHexKey(R.id.hexkey4, "4");
    	bindHexKey(R.id.hexkey5, "5");
    	bindHexKey(R.id.hexkey6, "6");
    	bindHexKey(R.id.hexkey7, "7");
    	bindHexKey(R.id.hexkey8, "8");
    	bindHexKey(R.id.hexkey9, "9");
    	bindHexKey(R.id.hexkeya, "a");
    	bindHexKey(R.id.hexkeyb, "b");
    	bindHexKey(R.id.hexkeyc, "c");
    	bindHexKey(R.id.hexkeyd, "d");
    	bindHexKey(R.id.hexkeye, "e");
    	bindHexKey(R.id.hexkeyf, "f");
    	bindHexKey(R.id.hexkeyspace, " ");
    	((Button)findViewById(R.id.hexkeydel)).setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			EditText hexEntry = (EditText) findViewById(R.id.entryHex);
    			int s,e;

    			s=hexEntry.getSelectionStart();
    			e=hexEntry.getSelectionEnd();

    			if (e<s) {
    				int t;
    				t=e;
    				e=s;
    				s=t;
    			}

    			if (s==e) {
    				if (s>0) {
    					hexEntry.getText().delete(s-1,s);		
    				}
    			} else {
    				hexEntry.getText().delete(s,e);
    			}
    		}
    	});
    };

    // --------------------------------------------------------------------------------------
    private void updateOptionList() {
    	GridView gv = (GridView) findViewById(R.id.optionsGrid);
    	
    	mArchVA = new SubArchOptViewAdapter(mContext, selectedSubarch.opts);
    	gv.setAdapter(mArchVA);
    }
    
    // --------------------------------------------------------------------------------------
    private String buildOptionString() {
      String result="";
      SubarchOption curOptions[]=selectedSubarch.opts;
      
      if (curOptions != null) {
          for (int i=0;i<curOptions.length; i++) {
        	  String newText="";
              if (curOptions[i].visible) {
                  // If it's visible then get the value from the check box
            	  newText = (mArchVA.isChecked(i)?curOptions[i].trueText:curOptions[i].falseText);
              } else {
            	  // If it's not visible just use the value from the default
            	
            	  newText = (curOptions[i].trueByDefault?curOptions[i].trueText:curOptions[i].falseText);
              }
              
              // Comma separate options
              if ((result.length() > 0) && (newText.length() > 0)) result=result+",";
         	  result=result+newText;
          }
      }
      
      return result;
    }
    
    private int buildOptionFlags() {
      int result=0;
      SubarchOption curOptions[]=selectedSubarch.opts;
      
      if (curOptions != null) {
          for (int i=0;i<curOptions.length; i++) {
              if (curOptions[i].visible) {
                  // If it's visible then get the value from the check box
            	  if (mArchVA.isChecked(i)) result|=curOptions[i].trueFlags;
              } else {
            	  // If it's not visible just use the value from the default
            	  if (curOptions[i].trueByDefault) result|=curOptions[i].trueFlags;
              }
          }
      }
      
      return result;
      
    }
    // --------------------------------------------------------------------------------------    
    // Called when the user clicmble
    private OnClickListener mDoDisassembleListener = new OnClickListener() {
        public void onClick(View v) {
        	CharSequence hexText;
        	byte[] bytes = null;
        	String[] errString={""};
        	String result;
        	int flags=buildOptionFlags();
        	
        	EditText asmEntry = (EditText) findViewById(R.id.entryAsm);
        	EditText hexEntry = (EditText) findViewById(R.id.entryHex);
      	  
        	hexText = hexEntry.getText();
        	
        	bytes = parseHexString(hexText.toString(), errString, (flags & SubarchOption.FlagBigEndian)!=0);
      	    
        	if (errString[0] == "") {
        		result = bug.doDisassemble(selectedArch.nameToDiss, selectedSubarch.nameToDiss,
          			  buildOptionString(), flags, bytes);
        	} else {
        		result = errString[0];
        	}
        	asmEntry.setText(result);
        }
    };
     
    // For when the user changes the arch selection
    private OnItemSelectedListener mArchSpinnerListener = new OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        selectedArch = archArray[pos];
        
        selectedSubarch = selectedArch.subDefs[0];
        mSubArchSpinner.setAdapter(new SubArchListAdapter(mContext,selectedArch.subDefs));
        updateOptionList();
      }
      
      public void onNothingSelected(AdapterView<?> parent) {
        // ignore for the moment
      }
    };
    
    // For when the user changes the subarch selection
    private OnItemSelectedListener mSubArchSpinnerListener = new OnItemSelectedListener() {
    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    		selectedSubarch = selectedArch.subDefs[pos];
    		updateOptionList();
    	}
    	
    	public void onNothingSelected(AdapterView<?> parent) {
    		// ignore for the moment
    	}
    };

    // --------------------------------------------------------------------------------------
    // Dialogs (mostly from the menu)
    private static final int DIALOGABOUT=1;
    private static final int DIALOGLICENSE=2;
    
    private Dialog doCreateTextFileDisplayDialog(String filename) {
      final Dialog dialog;
      
      dialog = new Dialog(this);
      dialog.setContentView(R.layout.textdisplay);
      dialog.setTitle(filename);
      
      Button closeBut=(Button)dialog.findViewById(R.id.tdclose);
      closeBut.setOnClickListener(new OnClickListener() {
    	  public void onClick(View v) {
            dialog.dismiss();
    	  }
      });
      // Based on code in Android apiDemos
      try {
        InputStream is=getAssets().open(filename);
        byte[] tmpbuf=new byte[is.available()];
        is.read(tmpbuf);
        is.close();
      
        String tmpstr=new String(tmpbuf);
        TextView txt=(TextView) dialog.findViewById(R.id.tdtext);
        txt.setText(tmpstr);
      } catch (IOException e) {
        return null;
      }
      
      return dialog;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch (id) {
    	case DIALOGABOUT:
    		dialog=doCreateTextFileDisplayDialog("README.txt");
    		break;
    		
    	case DIALOGLICENSE:
    		dialog=doCreateTextFileDisplayDialog("Licenses.txt");
    		break;
    		
        default:
        	  dialog=null;	
    	}
    	
    	return dialog;
    }
    // --------------------------------------------------------------------------------------
    // For the menu button
    private static final int MENUITEMABOUT=1;
    private static final int MENUITEMLICENSE=2;
    private static final int MENUITEMQUIT=3;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENUITEMABOUT:
            showDialog(DIALOGABOUT);
    		break;
    		
    	case MENUITEMLICENSE:
    		showDialog(DIALOGLICENSE);
    		break;
    		
    	case MENUITEMQUIT:
    		finish();
    		break;
    	}
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, MENUITEMABOUT, 0, R.string.about);
        menu.add(0, MENUITEMLICENSE, 0, R.string.license);
    	menu.add(0, MENUITEMQUIT, 0, R.string.quit);
    	return true;
    }


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        mContext = this;
        
        bug = new BinUtilsGlue();
        
        setContentView(R.layout.main);

        mDoDisassemble = (Button) findViewById(R.id.godisassemble);
        mDoDisassemble.setOnClickListener(mDoDisassembleListener);
        
        
        mArchSpinner = (Spinner) findViewById(R.id.archspinner);
        mArchSpinner.setAdapter(new ArchListAdapter(this, archArray));
        mArchSpinner.setOnItemSelectedListener(mArchSpinnerListener);
        
        mSubArchSpinner = (Spinner) findViewById(R.id.subarchspinner);
        mSubArchSpinner.setOnItemSelectedListener(mSubArchSpinnerListener);
        
        setupHexKeys();
               
    }
}
