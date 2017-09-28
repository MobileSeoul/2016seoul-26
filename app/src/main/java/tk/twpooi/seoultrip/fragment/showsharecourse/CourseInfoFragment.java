package tk.twpooi.seoultrip.fragment.showsharecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.DividerItemDecoration;

/**
 * Created by tw on 2016-08-16.
 */
public class CourseInfoFragment extends Fragment{


    // UI
    private View view;
    private Context context;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    // Data
    private ArrayList<HashMap<String, String>> attractionList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_show_share_course, container, false);
        context = container.getContext();

        attractionList = ((ShowShareCourseActivity)getActivity()).getAttractionList();

        init();
        makeList();

        return view;

    }

    private void init(){

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

    }

    private void makeList(){

        final CourseInfoCustomAdapter adapter = new CourseInfoCustomAdapter(context, attractionList, rv);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }


}
