// (c) 2012 Dr. David Alan Gilbert <dave@treblig.org>

package org.treblig.pocketdisassembler;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

//An adapter used to display the names in the arch spinner based on a list of ArchDef's
//it always returns the displayed name
public class ArchListAdapter implements SpinnerAdapter {

	public ArchListAdapter(Context c, ArchDef[] _defs) {
	  defs = _defs;
	  mContext = c;
	  mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		return getDropDownView(position, convertView, parent);
	}

	public int getCount() {
		return defs.length;
	}

	public Object getItem(int position) {
		return defs[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return getCount()==0;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView ourTV;
		
		if (convertView == null ) {
		    ourTV = (TextView) mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
		} else {
			ourTV = (TextView)convertView;
		}

		ourTV.setText(defs[position].nameDisplayed);
		
		return ourTV;
	}
	
	private ArchDef[] defs;
	private Context mContext;
	private LayoutInflater mInflater; // Based on ArrayAdapter's way of doing it
}
