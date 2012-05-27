// (c) 2012 Dr. David Alan Gilbert <dave@treblig.org>
package org.treblig.pocketdisassembler;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

// An adapter that gives a Grid view the set of options to twiddle
public class SubArchOptViewAdapter extends BaseAdapter {
    private Context mContext;
    private SubarchOption[] mOptions;
	private int nVisOptions; /* Number of options that are visible */
	private int visOptionMap[]; /* item [n] says that the 3rd visible option maps to item [n] in mOptions */
	private int revOptionMap[]; /* item [n] says that the nth item in mOptions is item [n] in buttons */
	private CheckBox buttons[]; /* The visible buttons */
	
	public SubArchOptViewAdapter(Context c, SubarchOption[] options) {
		int vis;
        mContext = c;
        mOptions = options;
        
        // 1st pass - how many visible options do we have
        nVisOptions = 0;
        for(int i=0; i<options.length;i++) {
        	if (options[i].visible) nVisOptions++;
        }
        
        visOptionMap=new int[nVisOptions];
        revOptionMap=new int[options.length];
        buttons=new CheckBox[nVisOptions];

        // Build our maps
        vis=0;
        for(int i=0; i<options.length;i++) {
        	if (options[i].visible) {
        		visOptionMap[vis] = i;
        		revOptionMap[i] = vis;
        		vis++;
        	}
        }
	}

	public int getCount() {
		return nVisOptions;
	}

	public Object getItem(int position) {
		return buttons[position];
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		CheckBox ourButton;
		int optIndex = visOptionMap[position];
		
		if (convertView == null) {
		    ourButton = new CheckBox(mContext);
		} else {
			ourButton = (CheckBox) convertView;
		}

	    ourButton.setText(mOptions[optIndex].name);
        ourButton.setChecked(mOptions[optIndex].trueByDefault);
        
		buttons[optIndex]=ourButton;
		
		return ourButton;
	}

	// This is indexed by the option index in the option array passed in and tells
	// the caller if the option is selected
	public Boolean isChecked(int opt) {
		return buttons[revOptionMap[opt]].isChecked();
	}
}
