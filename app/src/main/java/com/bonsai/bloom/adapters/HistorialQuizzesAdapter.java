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

public class HistorialQuizzesAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public HistorialQuizzesAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_quizz_historial, parent, false);
	        holder.title = (TextView) rowView.findViewById(R.id.textTitulo);
			holder.tema = (TextView) rowView.findViewById(R.id.textTema);
			holder.puntos = (TextView) rowView.findViewById(R.id.textPuntaje);
			holder.fecha = (TextView) rowView.findViewById(R.id.textFecha);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.title.setText(Html.fromHtml(jsonOb.getString("quizz")));
				holder.tema.setText(Html.fromHtml("Tema: " + jsonOb.getString("tema")));
				holder.puntos.setText(Html.fromHtml("Puntaje: " + jsonOb.getString("puntaje")));
				holder.fecha.setText(Html.fromHtml(jsonOb.getString("fecha")));
				holder.hidden.setText(jsonOb.getString("idquizz"));
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }
		
		static class Holder {
			TextView title;
			TextView tema;
			TextView puntos;
			TextView fecha;
	        TextView hidden;
		}
}