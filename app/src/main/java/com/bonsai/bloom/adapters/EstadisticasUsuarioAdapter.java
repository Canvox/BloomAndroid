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

public class EstadisticasUsuarioAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public EstadisticasUsuarioAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_estadistica_usuario, parent, false);
	        holder.titulo = (TextView) rowView.findViewById(R.id.textTitulo);
			holder.tiempo = (TextView) rowView.findViewById(R.id.textTiempo);
			holder.fecha = (TextView) rowView.findViewById(R.id.textFecha);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.titulo.setText(jsonOb.getString("ventana"));
				holder.tiempo.setText((String.valueOf(round(Double.parseDouble(jsonOb.getString("tiempo")) / 60 , 2))) + " minutos");
				holder.fecha.setText(jsonOb.getString("fecha"));
				holder.hidden.setText(jsonOb.toString());
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
			TextView titulo;
			TextView tiempo;
			TextView fecha;
	        TextView hidden;
		}
}