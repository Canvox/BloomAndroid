package com.bonsai.bloom.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bonsai.bloom.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class EstadisticasAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public EstadisticasAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_estadista, parent, false);
	        holder.nombre = (TextView) rowView.findViewById(R.id.textNombre);
			holder.tiempo = (TextView) rowView.findViewById(R.id.textTiempo);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.nombre.setText(jsonOb.getString("nombres") + " " + jsonOb.getString("apellidos"));
				holder.tiempo.setText((String.valueOf(round(Double.parseDouble(jsonOb.getString("tiempo")) / 60 , 2))) + " minutos");
				holder.hidden.setText(jsonOb.getString("idusuario"));
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }

		public static double round(double value, int places) {
			if (places < 0) throw new IllegalArgumentException();

			long factor = (long) Math.pow(10, places);
			value = value * factor;
			long tmp = Math.round(value);
			return (double) tmp / factor;
		}

		static class Holder {
			TextView nombre;
			TextView tiempo;
	        TextView hidden;
		}
}