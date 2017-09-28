package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;

/**
 * Created by tw on 2016-08-17.
 */
public class FavoriteListFragment extends Fragment {

    // Database
    DBHelper dbHelper;

    private FileManager fileManager;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionsMenu menu;
    private Context context;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<HashMap<String,String>> attractionList;
    private FavoriteListCustomAdapter adapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_favorite_list, container, false);
        context = container.getContext();
        mToolbar = ((MainTabActivity)getActivity()).getToolbar();

        fileManager = new FileManager(context);

        initUI();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);
        attractionList = dbHelper.getResultFavorite(StartActivity.DATABASE_LANGUAGE);

        makeList();

        return view;

    }

    public void makeList(){

        int scrollX = rv.computeHorizontalScrollOffset();
        int scrollY = rv.computeVerticalScrollOffset();

        ArrayList<HashMap<String, String>> list = fileManager.readFavorite();//dbHelper.getResultFavorite(StartActivity.DATABASE_LANGUAGE);

        TextView nonFavorite = (TextView)view.findViewById(R.id.non_favorite);
        if(list.size()==0){
            nonFavorite.setVisibility(View.VISIBLE);
        }else{
            nonFavorite.setVisibility(View.GONE);
        }

        adapter = new FavoriteListCustomAdapter(context, list, rv, mToolbar, this);

        rv.setAdapter(adapter);

        rv.scrollBy(scrollX, scrollY);

        adapter.notifyDataSetChanged();

    }

    public void initUI(){

        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        FloatingActionButton showMapBtn = (FloatingActionButton) view.findViewById(R.id.show_map);
        showMapBtn.setOnClickListener(getOnClickListener());


    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }


    public void refreshFragment(){

        makeList();

    }

    public View.OnClickListener getOnClickListener(){

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(context, ShowMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("attraction_favorite", fileManager.readFavorite());
                context.startActivity(intent);
            }
        };

        return onClickListener;

    }


}
