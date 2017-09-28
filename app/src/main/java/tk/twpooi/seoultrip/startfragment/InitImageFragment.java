package tk.twpooi.seoultrip.startfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class InitImageFragment extends Fragment{


    // UI
    private View view;
    private Context context;

    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
//        image = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        int[] resource = {
                R.drawable.attraction_introduce,
                R.drawable.mycourse,
                R.drawable.share_course,
                R.drawable.favorite_attraction,
                R.drawable.course_game
        };

        int[] titleResource = {
                R.string.init_introduce_attraction,
                R.string.init_my_course,
                R.string.init_share_course,
                R.string.init_favorite,
                R.string.init_course_game
        };

        int[] contentResource = {
                R.string.init_intro_contents,
                R.string.init_my_contents,
                R.string.init_share_contents,
                R.string.init_favorite_contents,
                R.string.init_game_contents
        };

        // UI
        view = inflater.inflate(R.layout.fragment_start_init_image, container, false);
        context = container.getContext();

        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(titleResource[position]);

        TextView contents = (TextView)view.findViewById(R.id.contents);
        contents.setText(contentResource[position]);

        ImageView imageView = (ImageView)view.findViewById(R.id.img);
        imageView.setImageResource(resource[position]);

        return view;

    }

    @Override
    public void onStart(){
        super.onStart();
    }


}
