package tk.twpooi.seoultrip.fragment.showsharecourse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class CourseInfoCustomAdapter extends RecyclerView.Adapter<CourseInfoCustomAdapter.ViewHolder> {

    // UI
    private Context context;

    // Data
    private ArrayList<HashMap<String, String>> attractionList;


    // 생성자
    public CourseInfoCustomAdapter(Context context, ArrayList<HashMap<String, String>> attractionList, RecyclerView recyclerView) {

        this.context = context;
        this.attractionList = attractionList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_share_course_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int pos = position;

        HashMap<String, String> db = attractionList.get(pos);

        // set circle crop image
        String imageUrl = db.get("mainImage");
        Picasso.with(context).load(imageUrl)
                .transform(new CropCircleTransformation()).into(holder.image);

        // set sContents
        String sContents = db.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_content.setText(sContents);

        holder.tv_title.setText(Integer.toString(pos+1) + ". " + db.get("title")); //제목

    }

    @Override
    public int getItemCount() {
        return this.attractionList.size();
    }

    public final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_content;
        ImageView image;

        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_content = (TextView)v.findViewById(R.id.tv_content);
            image = (ImageView)v.findViewById(R.id.image);
        }
    }


}
