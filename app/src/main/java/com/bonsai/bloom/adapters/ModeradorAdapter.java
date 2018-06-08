package com.bonsai.bloom.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bonsai.bloom.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModeradorAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public ModeradorAdapter(Context c, JSONArray values) {
	        this.mContext = c;
	        this.values = values;
	    }
	 
	    public int getCount() {
	        return values.length();
	    }
	 
	    public Object getItem(int position) {
	        return null;
	    }
	 
	    public long getItemId(int position) {
	        return 0;
	    }
	    
	    public View getView(int position, View rowView, ViewGroup parent) {
	    	Holder holder;
		    holder = new Holder();
		    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.list_moderador, parent, false);
	        holder.nombre = (TextView) rowView.findViewById(R.id.textNombre);
			holder.facultad = (TextView) rowView.findViewById(R.id.textFacultad);
			holder.carrera = (TextView) rowView.findViewById(R.id.textCarrera);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.nombre.setText(Html.fromHtml(jsonOb.getString("nombres") + " " + jsonOb.getString("apellidos")));
				holder.facultad.setText(Html.fromHtml(jsonOb.getString("facultad")));
				holder.carrera.setText(Html.fromHtml(jsonOb.getString("carrera")));
				holder.hidden.setText(jsonOb.getString("tag"));

				if(jsonOb.getString("selected").equals("1")) {
					rowView.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
				} else {
					rowView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
				}
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }
		
		static class Holder {
			TextView nombre;
			TextView facultad;
			TextView carrera;
	        TextView hidden;
		}
}