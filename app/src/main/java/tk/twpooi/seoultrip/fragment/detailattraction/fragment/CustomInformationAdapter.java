package tk.twpooi.seoultrip.fragment.detailattraction.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import tk.twpooi.seoultrip.R;

/**
 * Created by TaeWoo on 16. 8. 1..
 */
public class CustomInformationAdapter extends BaseAdapter {

    private ArrayList<String[]> m_List;

    private FlexibleSpaceWithImageListViewFragment f;

    public CustomInformationAdapter(FlexibleSpaceWithImageListViewFragment f){
        m_List = new ArrayList<String[]>();
        this.f = f;
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

        TextView infoTitle = null;
        TextView infoContents = null;
        CustomHolder holder = null;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_information_list_item, parent, false);

            infoTitle = (TextView)convertView.findViewById(R.id.infoTitle);
            infoContents = (TextView)convertView.findViewById(R.id.infoContents);

            holder = new CustomHolder();
            holder.infoTitle = infoTitle;
            holder.infoContents = infoContents;

            convertView.setTag(holder);

        }else{
            holder = (CustomHolder)convertView.getTag();
            infoTitle = holder.infoTitle;
            infoContents = holder.infoContents;
        }

        final String title = m_List.get(position)[0];
        String contents = m_List.get(position)[1];
        if(title == null) {
            infoTitle.setText(context.getResources().getString(R.string.no_information));
        }else{
            infoTitle.setText(title);
        }

        if(contents == null || contents.equals("")) {
            infoContents.setText(context.getResources().getString(R.string.no_information));
        }else{
            infoContents.setText(contents);
        }

        return convertView;
    }

    public void add(String[] info){
        m_List.add(info);
    }

    public void remove(int position){
        m_List.remove(position);
    }

    private class CustomHolder{
        TextView infoTitle;
        TextView infoContents;
    }

}
