package com.bonsai.bloom.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bonsai.bloom.FragmentListaQuizzesBloomHabilitar;
import com.bonsai.bloom.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuizzesHabilitaAdapter extends BaseAdapter {
	    private Context mContext;
	    private final JSONArray values;



	    public QuizzesHabilitaAdapter(Context c, JSONArray values) {
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
			rowView = inflater.inflate(R.layout.list_quizz_historial_habilita, parent, false);
	        holder.title = (TextView) rowView.findViewById(R.id.textTitulo);
			holder.tema = (TextView) rowView.findViewById(R.id.textTema);
			holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);
			//holder.switchHabilita = ((Switch) rowView.findViewById(R.id.switchHabilita));
			holder.checkBoxEst = ((CheckBox) rowView.findViewById(R.id.checkBoxEst));
			holder.checkBoxEst.setEnabled(false);



			try {
				JSONObject jsonOb = values.getJSONObject(position);
				holder.title.setText(Html.fromHtml(jsonOb.getString("descripcion")));
				holder.tema.setText(Html.fromHtml("Tema: " + jsonOb.getString("tema")));
				holder.hidden.setText(jsonOb.getString("idquizz"));

				if(jsonOb.get("habilitado").equals("1"))
					holder.checkBoxEst.setChecked(true);


			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}


			rowView.setTag(holder);
			return rowView;
	    }

		
		static class Holder {
			TextView title;
			TextView tema;
	        TextView hidden;
			CheckBox checkBoxEst;
			//Switch switchHabilita;
		}
}