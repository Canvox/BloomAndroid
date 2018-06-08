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

public class CursosAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public CursosAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_curso, parent, false);
	        holder.title = (TextView) rowView.findViewById(R.id.textTitulo);
			holder.profesor = (TextView) rowView.findViewById(R.id.textProfesor);
			holder.integrantes = (TextView) rowView.findViewById(R.id.textIntegrantes);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);
			holder.hidden2 = (TextView) rowView.findViewById(R.id.textHidden2);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.title.setText(Html.fromHtml(jsonOb.getString("nombre")));
				holder.profesor.setText(Html.fromHtml("Profesor: " + jsonOb.getString("nombreprof")));
				holder.integrantes.setText(Html.fromHtml("Integrantes: " + jsonOb.getString("miembros")));
				holder.hidden.setText(jsonOb.getString("idgrupo"));
				holder.hidden2.setText(jsonOb.getString("idmoderador"));
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }
		
		static class Holder {
			TextView title;
			TextView profesor;
			TextView integrantes;
	        TextView hidden;
			TextView hidden2;
		}
}
