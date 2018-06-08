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

public class HistorialTemasAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public HistorialTemasAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_historial_tema, parent, false);
	        holder.title = (TextView) rowView.findViewById(R.id.textTitulo);
			holder.puntaje = (TextView) rowView.findViewById(R.id.textPuntaje);
			holder.posicion = (TextView) rowView.findViewById(R.id.textPosicion);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.title.setText(Html.fromHtml(jsonOb.getString("tema")));
				holder.puntaje.setText("Puntaje: " + Html.fromHtml(jsonOb.getString("puntaje")));
				holder.posicion.setText(Html.fromHtml(jsonOb.getString("posicion")));
				holder.hidden.setText(jsonOb.toString());
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }
		
		static class Holder {
			TextView title;
			TextView puntaje;
	        TextView posicion;
			TextView hidden;
		}
}