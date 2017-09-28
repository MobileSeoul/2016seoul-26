package tk.twpooi.seoultrip.fragment.detailattraction;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ppamorim.dragger.DraggerPosition;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import tk.twpooi.seoultrip.CustomViewPager;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.AddCourseListActivity;
import tk.twpooi.seoultrip.fragment.AddMyCourseActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.fragment.FlexibleSpaceWithImageBaseFragment;
import tk.twpooi.seoultrip.fragment.detailattraction.fragment.FlexibleSpaceWithImageImageListViewFragment;
import tk.twpooi.seoultrip.fragment.detailattraction.fragment.FlexibleSpaceWithImageListViewFragment;
import tk.twpooi.seoultrip.fragment.detailattraction.fragment.FlexibleSpaceWithImageScrollViewFragment;
import tk.twpooi.seoultrip.fragment.detailattraction.widget.SlidingTabLayout;

/**
 * Created by tw on 2016-08-16.
 */
public class DetailActivity extends BaseActivity {

    // UI
    static private CustomViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private int mFlexibleSpaceHeight;
    private int mTabHeight;
    protected static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    private FloatingActionsMenu menu;
    private FloatingActionButton favoriteBtn;

    // Database
    private DBHelper dbHelper;
    public static String dbLanguage;
    private FileManager fileManager;

    // Daum map

    private String title;
    private String contents;
    private ArrayList<String> imageUrl;
    private String mainImage;
    private ArrayList<String[]> informationList;
    private ArrayList<String> categorize;
    static public Double lng;
    static public Double lat;

    static public boolean isAddress = false;
    static public boolean noMapView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatil_attraction);

        // Database
        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        Intent i = getIntent();
        title = i.getStringExtra("title");
        noMapView = i.getBooleanExtra("noMapView", false);
        dbLanguage = i.getStringExtra("language");
        if(dbLanguage == null){
            dbLanguage = StartActivity.DATABASE_LANGUAGE;
        }

        if(!dbLanguage.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(title, dbLanguage);
            if(tempTitle != null){
                dbLanguage = StartActivity.DATABASE_LANGUAGE;
                title = tempTitle;
            }
        }

        fileManager = new FileManager(getApplicationContext());

        // UI
        String[] TITLES = {
                getResources().getString(R.string.intro),
                getResources().getString(R.string.addition_info),
                getResources().getString(R.string.picture)
        };
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), TITLES);
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(3);
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mTabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.colorAccent));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mPager);

        // Initialize the first Fragment's state when layout is completed.
        ScrollUtils.addOnGlobalLayoutListener(mSlidingTabLayout, new Runnable() {
            @Override
            public void run() {
                translateTab(0, false);
            }
        });



        informationList = new ArrayList<>();

        madeContents();

        floationMenu();

    }

    public void floationMenu(){
        menu = (FloatingActionsMenu)findViewById(R.id.visit_multiple_actions);

        favoriteBtn = (FloatingActionButton)findViewById(R.id.favorite_btn);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isContain(fileManager.readFavorite(), title)){

                    ArrayList<HashMap<String, String>> favoriteList = fileManager.readFavorite();
                    int index = isIndex(favoriteList, title);
                    favoriteList.remove(index);
                    fileManager.writeFavorite(favoriteList);

                    setFavoriteBtn();
                    showSnackbar(title  + "\n" + getResources().getString(R.string.remove_favorite));
                    menu.toggle();

                }else{

                    ArrayList<HashMap<String, String>> favoriteList = fileManager.readFavorite();
                    HashMap<String, String> temp = new HashMap<String, String>();
                    temp.put("language", dbLanguage);
                    temp.put("title", title);
                    favoriteList.add(temp);
                    fileManager.writeFavorite(favoriteList);

                    setFavoriteBtn();
                    showSnackbar(title  + "\n" + getResources().getString(R.string.add_favorite));
                    menu.toggle();

                }

            }
        });
        setFavoriteBtn();

        FloatingActionButton addCourse = (FloatingActionButton)findViewById(R.id.add_course);
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCourseListDraggerActivity(DraggerPosition.TOP, title);
                menu.toggle();
            }
        });
        addCourse.setTitle(getResources().getString(R.string.add_to_my_course));

    }
    private int isIndex(ArrayList<HashMap<String, String>> list, String search){

        for(int i=0; i<list.size(); i++){
            if(list.get(i).get("title").equals(search)){
                return i;
            }
        }

        return -1;
    }

    private boolean isContain(ArrayList<HashMap<String, String>> list, String search){

        for(HashMap<String, String> temp : list){
            if(temp.get("title").equals(search)){
                return true;
            }
        }

        return false;

    }

    private void setFavoriteBtn(){
        if(isContain(fileManager.readFavorite(), title)){
            favoriteBtn.setTitle(getResources().getString(R.string.fab_remove_favorite));
            favoriteBtn.setColorNormalResId(R.color.yellow);
        }else{
            favoriteBtn.setTitle(getResources().getString(R.string.fab_add_favorite));
            favoriteBtn.setColorNormalResId(R.color.dark_gray);
        }
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    static public void setViewPagerScrollEnabled(boolean enabled){
        mPager.setPagingEnabled(enabled);
    }

    public Double getLng(){
        return lng;
    }
    public Double getLat(){
        return lat;
    }
    public String getDbLanguage(){
        return dbLanguage;
    }
    public String getAttractionTitle(){
        return title;
    }
    public String getContents(){
        return contents;
    }
    public ArrayList<String> getImageUrl(){
        return imageUrl;
    }
    public ArrayList<String[]> getInformationList(){
        return informationList;
    }
    public ArrayList<String> getCategorize(){
        return categorize;
    }

    public void madeContents(){

        HashMap<String, String> list = dbHelper.getResultSearchTitle(title, dbLanguage).get(0);

        contents = list.get("contents");
        mainImage = list.get("mainImage");
        String categorizeTemp[] = list.get("categorize").split(",");
        try {
            lat = Double.parseDouble(list.get("lat"));
            lng = Double.parseDouble(list.get("lng"));
        }catch (Exception e){
        }

        categorize = new ArrayList<>();
        for(String s : categorizeTemp){
            if(!s.equals("")) {
                categorize.add(s);
            }
        }

        ImageView image = (ImageView)findViewById(R.id.image);
        Picasso.with(getApplicationContext()).load(mainImage)
                .transform(new BlurTransformation(getApplicationContext(), 3))
                .into(image);

        TextView titleView = (TextView)findViewById(R.id.title);
        titleView.setText(title);

        imageUrl = new ArrayList<>();
        imageUrl.add(mainImage);
        String subImage = list.get("subImage");
        String[] tempImage = subImage.split(",");
        for(String s : tempImage){
            imageUrl.add(s);
        }

        madeInformationList(list);

    }


    public void madeInformationList(HashMap<String, String> list){

        if(list.get("address").equals("") || list.get("address") == null){
            isAddress = false;
        }
        else {
            String[] temp = new String[2];
            temp[0] = getResources().getString(R.string.address);
            temp[1] = list.get("address");
            informationList.add(temp);
            isAddress = true;
        }
        if(list.get("telephone").equals("") || list.get("telephone") == null){}
        else {
            String[] temp = new String[2];
            temp[0] = getResources().getString(R.string.telephone);
            temp[1] = list.get("telephone");
            informationList.add(temp);
        }
        if(list.get("web").equals("") || list.get("web") == null){}
        else {
            String[] temp = new String[2];
            temp[0] = getResources().getString(R.string.website);
            temp[1] = list.get("web");
            informationList.add(temp);
        }
        if(list.get("detail").equals("") || list.get("detail") == null){}
        else{
            String detail = list.get("detail");
            String[] details = detail.split("&&");

            for(String d : details){
                String[] ds = d.split("##");
                informationList.add(ds);
            }

        }
        if(list.get("url").equals("") || list.get("url") == null){}
        else{
            String[] temp = new String[2];
            temp[0] = getResources().getString(R.string.source);
            temp[1] = list.get("url");
            informationList.add(temp);

        }

    }


    @Override
    public void onBackPressed() {
        if (menu.isExpanded()) {
            menu.toggle();
        } else {
            super.onBackPressed();
        }
    }

    public void startCourseListDraggerActivity(DraggerPosition dragPosition, String attraction) {
        Intent intent = new Intent(getApplicationContext(), AddCourseListActivity.class);
        intent.putExtra(AddMyCourseActivity.DRAG_POSITION, dragPosition);
        intent.putExtra("attraction", attraction);
        intent.putExtra("language", dbLanguage);
        startActivityNoAnimation(intent);
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void onScrollChanged(int scrollY, Scrollable s) {
        FlexibleSpaceWithImageBaseFragment fragment =
                (FlexibleSpaceWithImageBaseFragment) mPagerAdapter.getItemAt(mPager.getCurrentItem());
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }
        Scrollable scrollable = (Scrollable) view.findViewById(R.id.scroll);
        if (scrollable == null) {
            return;
        }
        if (scrollable == s) {
            // This method is called by not only the current fragment but also other fragments
            // when their scrollY is changed.
            // So we need to check the caller(S) is the current fragment.
            int adjustedScrollY = Math.min(scrollY, mFlexibleSpaceHeight - mTabHeight);
            translateTab(adjustedScrollY, false);
            propagateScroll(adjustedScrollY);
        }
    }

    private void translateTab(int scrollY, boolean animated) {
        int flexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        View imageView = findViewById(R.id.image);
        View overlayView = findViewById(R.id.overlay);
        TextView titleView = (TextView) findViewById(R.id.title);

        // Translate overlay and image
        float flexibleRange = flexibleSpaceImageHeight - getActionBarSize();
        int minOverlayTransitionY = tabHeight - overlayView.getHeight();
        ViewHelper.setTranslationY(overlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(imageView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        ViewHelper.setAlpha(overlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY - tabHeight) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        setPivotXToTitle(titleView);
        ViewHelper.setPivotY(titleView, 0);
        ViewHelper.setScaleX(titleView, scale);
        ViewHelper.setScaleY(titleView, scale);

        // Translate title text
        int maxTitleTranslationY = flexibleSpaceImageHeight - tabHeight - getActionBarSize();
        int titleTranslationY = maxTitleTranslationY - scrollY;
        ViewHelper.setTranslationY(titleView, titleTranslationY);

        // If tabs are moving, cancel it to start a new animation.
        ViewPropertyAnimator.animate(mSlidingTabLayout).cancel();
        // Tabs will move between the top of the screen to the bottom of the image.
        float translationY = ScrollUtils.getFloat(-scrollY + mFlexibleSpaceHeight - mTabHeight, 0, mFlexibleSpaceHeight - mTabHeight);
        if (animated) {
            // Animation will be invoked only when the current tab is changed.
            ViewPropertyAnimator.animate(mSlidingTabLayout)
                    .translationY(translationY)
                    .setDuration(200)
                    .start();
        } else {
            // When Fragments' scroll, translate tabs immediately (without animation).
            ViewHelper.setTranslationY(mSlidingTabLayout, translationY);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setPivotXToTitle(View view) {
        final TextView mTitleView = (TextView) view.findViewById(R.id.title);
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setPivotX(mTitleView, view.findViewById(android.R.id.content).getWidth());
        } else {
            ViewHelper.setPivotX(mTitleView, 0);
        }
    }

    private void propagateScroll(int scrollY) {
        // Set scrollY for the fragments that are not created yet
        mPagerAdapter.setScrollY(scrollY);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            FlexibleSpaceWithImageBaseFragment f =
                    (FlexibleSpaceWithImageBaseFragment) mPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }
            f.setScrollY(scrollY, mFlexibleSpaceHeight);
            f.updateFlexibleSpace(scrollY);
        }
    }

    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private String[] TITLES;// = new String[]{"소개", "부가정보", "사진"};

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm, String[] TITLES) {
            super(fm);
            this.TITLES = TITLES;
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            FlexibleSpaceWithImageBaseFragment f;
            final int pattern = position % 4;
            switch (pattern) {
                case 0: {
                    f = new FlexibleSpaceWithImageScrollViewFragment();
                    break;
                }
                case 1: {
                    f = new FlexibleSpaceWithImageListViewFragment();
                    break;
                }
                case 2: {
                    f = new FlexibleSpaceWithImageImageListViewFragment();
                    break;
                }
                case 3:
                default: {
                    f = new FlexibleSpaceWithImageScrollViewFragment();
                    break;
                }
            }
            f.setArguments(mScrollY);
            return f;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
    }




}
