package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class DetailCourseGameFragment extends Fragment{

    // UI
    private View view;
    private Context context;

    private TextView mNum;
    private ImageView mImageView;
    private TextView titleText;
    private TextView detailAddress;
    private RelativeLayout rl_overlay;

    // DB
    private DBHelper dbHelper;


    private int position;
    private HashMap<String, String> attractionDB;
    private String title;
    private String language;
    private String mainImage;
    private String address;
    private boolean isOverlay = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        title = (String)getArguments().getSerializable("title");
        position = getArguments().getInt("position");
        isOverlay = getArguments().getBoolean("visiable", false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_detail_course_game, container, false);
        context = container.getContext();

        language = ((DetailCourseGameActivity)getActivity()).getLanguage();

        initData();
        initUI();

        return view;

    }

    private void initData(){

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        if(!language.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(title, language);
            if(tempTitle != null){
                language = StartActivity.DATABASE_LANGUAGE;
                title = tempTitle;
            }
        }

        attractionDB = dbHelper.getResultSearchTitle(title, language).get(0);

        mainImage = attractionDB.get("mainImage");
        address = attractionDB.get("address");

    }

    private void initUI(){

        RelativeLayout root = (RelativeLayout)view.findViewById(R.id.rl_root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("title", title);
                i.putExtra("language", language);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        mNum = (TextView)view.findViewById(R.id.num);
        mNum.setText((position+1) + "번째 목표");

        mImageView = (ImageView)view.findViewById(R.id.img);
        Picasso.with(context).load(mainImage)
                .into(mImageView);

        titleText = (TextView)view.findViewById(R.id.title);
        titleText.setText(title);

        detailAddress = (TextView)view.findViewById(R.id.detail_address);
        if(address.equals("") || address == null){
            detailAddress.setText(getResources().getText(R.string.no_address));
        }else{
            detailAddress.setText(address);
        }

        rl_overlay = (RelativeLayout)view.findViewById(R.id.rl_overlay);
        if(isOverlay){
            setOverlayVisiable();
        }

    }

    public void setOverlayVisiable(){
        rl_overlay.setVisibility(View.VISIBLE);
    }

}
