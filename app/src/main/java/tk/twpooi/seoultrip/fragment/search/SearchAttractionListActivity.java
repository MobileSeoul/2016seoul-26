package tk.twpooi.seoultrip.fragment.search;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.AttractionListCustomAdapter;

/**
 * Created by tw on 2016-08-17.
 */
public class SearchAttractionListActivity extends AppCompatActivity {

    public static final String DRAG_POSITION = "drag_position";

    // Database
    private DBHelper dbHelper;

    private DraggerView draggerView;
    private Toolbar toolbar;
    private SearchView searchView;
    private TextView noticeMessage;


    private String finalQuery = "";


    // Recycle View
    private ObservableRecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    private void clearSingleton(){
        try{
            Field field = DraggerView.class.getDeclaredField("singleton");
            field.setAccessible(true);
            field.set(this, null);
        }catch(NoSuchFieldException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_search_attraction_list);

        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        init();

    }

    private void changeSearchViewTextColor(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
                return;
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #efefef>" + getResources().getString(R.string.please_enter_search_contents) + "</font>"));
        changeSearchViewTextColor(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                finalQuery = query;
                ArrayList<HashMap<String, String>> tempList = dbHelper.getResultAllBySearchQuery(query, StartActivity.DATABASE_LANGUAGE);
                if(tempList.size() == 0){
                    setNoticeMessage(true);
                    showSnackbar(getResources().getString(R.string.no_search_result));
                }else{
                    setNoticeMessage(false);
                    showSnackbar(tempList.size() + getResources().getString(R.string.search_result_count));
                }
                makeList(tempList);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        SearchManager searchManager = (SearchManager)this.getSystemService(Context.SEARCH_SERVICE);
        if(searchManager != null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);

        return true;
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton(); // 프레임 중복 해결
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (ObservableRecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);
        rv.setScrollViewCallbacks(onObservableScrollViewCallbacks);


        noticeMessage = (TextView)findViewById(R.id.message_notice);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);

    }

    public void setNoticeMessage(boolean check){
        if(check){
            noticeMessage.setVisibility(View.VISIBLE);
            noticeMessage.setText(getResources().getString(R.string.no_search_result));
            RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.background_color));
        }else{
            noticeMessage.setVisibility(View.GONE);
            RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.list_background_color));
        }
    }

    public void makeList(ArrayList<HashMap<String, String>> list){

        final AttractionListCustomAdapter adapter = new AttractionListCustomAdapter(getApplicationContext(), list, rv, toolbar, null);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private ObservableScrollViewCallbacks onObservableScrollViewCallbacks =
            new ObservableScrollViewCallbacks() {
                @Override public void onScrollChanged(int scrollY, boolean firstScroll,
                                                      boolean dragging) {
                    draggerView.setSlideEnabled(scrollY == 0);
                }
                @Override public void onDownMotionEvent() { }
                @Override public void onUpOrCancelMotionEvent(ScrollState scrollState) { }
            };


    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

}
