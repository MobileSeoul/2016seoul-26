package tk.twpooi.seoultrip.fragment.detailattraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.AttractionListCustomAdapter;

/**
 * Created by tw on 2016-08-16.
 */
public class SimilarAttractionActivity extends Activity {

    private RelativeLayout root;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private AttractionListCustomAdapter adapter;

    private ArrayList<HashMap<String, String>> attractionList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_similar_attraction);

        Intent intent = getIntent();
        attractionList = (ArrayList<HashMap<String, String>>)intent.getSerializableExtra("attraction");

        init();

        makeList();

    }

    private void init(){

        root = (RelativeLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);


    }

    private void makeList(){

        adapter = new AttractionListCustomAdapter(getApplicationContext(), attractionList, rv, null, null);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

}
