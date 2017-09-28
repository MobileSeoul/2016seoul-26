package tk.twpooi.seoultrip.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;

import java.util.ArrayList;

import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-15.
 */
public class ShowImageActivity extends AppCompatActivity {

    // UI
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TextView closeBtn;
    private RelativeLayout rl_background;
    private ViewPager viewPager;
    private NavigationAdapter mPagerAdapter;

    private String title = "image";
    private ArrayList<String> imageList;
    private int position;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        setStatusColor();

        initData();

        initUI();

    }


    private void initData(){
        Intent intent = getIntent();

        imageList = (ArrayList<String>)intent.getSerializableExtra("image");
        position = intent.getIntExtra("position", 0);
        title = intent.getStringExtra("title");

    }

    private void initUI() {

        rl_background = (RelativeLayout)findViewById(R.id.layout_background);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
        closeBtn = (TextView)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(imageList.size());
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), imageList, imageList.size());
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(position);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }


    public Toolbar getToolbar(){
        return this.toolbar;
    }
    public void hideToolbar() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        setStatusColorDark();
    }
    public void showToolbar() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        setStatusColor();
    }

    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private int size;
        private ArrayList<String> list;

        public NavigationAdapter(FragmentManager fm, ArrayList<String> list, int size){
            super(fm);
            this.size = size;
            this.list = list;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %size;

            f = new ShowImageFragment();
            Bundle bdl = new Bundle(1);
            bdl.putSerializable("url", list.get(pattern));
            f.setArguments(bdl);

            return f;
        }

        @Override
        public int getCount(){
            return size;
        }

    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
    }

    private void setStatusColorDark(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
    }

}
