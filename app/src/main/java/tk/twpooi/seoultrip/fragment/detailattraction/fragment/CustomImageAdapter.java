package tk.twpooi.seoultrip.fragment.detailattraction.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tk.twpooi.seoultrip.R;

/**
 * Created by TaeWoo on 16. 8. 1..
 */
public class CustomImageAdapter extends BaseAdapter {

    private ArrayList<String> m_List;

    public CustomImageAdapter(){
        m_List = new ArrayList<String>();
    }

    @Override
    public int getCount(){
        return m_List.size();
    }

    @Override
    public Object getItem(int position){
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        ImageView imageView = null;
        CustomHolder holder = null;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_image_list_item, parent, false);

            imageView = (ImageView)convertView.findViewById(R.id.listImageView);
            //Picasso.with(context).load(m_List.get(position)).into(imageView);

            holder = new CustomHolder();
            holder.imageView = imageView;
            convertView.setTag(holder);

        }else{
            holder = (CustomHolder)convertView.getTag();
            imageView = holder.imageView;
        }

        Picasso.with(context).load(m_List.get(position)).into(imageView);

        return convertView;
    }

    public void add(String url){
        m_List.add(url);
    }

    public void remove(int position){
        m_List.remove(position);
    }

    private class CustomHolder{
        ImageView imageView;
    }

}
