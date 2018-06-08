package com.bonsai.bloom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonsai.bloom.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

public class ImagenesAdapter extends BaseAdapter{
	    private Context mContext;
	    private final JSONArray values;

	    public ImagenesAdapter(Context c, JSONArray values) {
	        this.mContext = c;
	        this.values = values;
	    }

        protected ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icono_mas)
                .showImageOnFail(R.drawable.icono_mas)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
	 
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
			rowView = inflater.inflate(R.layout.list_imagen, parent, false);
	        holder.image = (ImageView) rowView.findViewById(R.id.imageView);
            holder.hidden = (TextView) rowView.findViewById(R.id.textHidden);

			try {
				JSONObject jsonOb = values.getJSONObject(position);
                imageLoader.displayImage(jsonOb.getString("url"), holder.image, options);
				holder.hidden.setText(jsonOb.getString("url"));
			} catch(Exception e) {
				Log.e(mContext.getResources().getString(R.string.app_name), mContext.getResources().getString(R.string.error_tag), e);
			}
			rowView.setTag(holder);
			return rowView;
	    }
		
		static class Holder {
			ImageView image;
            TextView hidden;
		}
}