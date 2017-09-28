package tk.twpooi.seoultrip;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.gigamole.navigationtabbar.ntb.NavigationTabBar;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.guillotine.animation.GuillotineAnimation;
import com.yalantis.guillotine.interfaces.GuillotineListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.fragment.AttractionListFragment;
import tk.twpooi.seoultrip.fragment.CourseGameFragment;
import tk.twpooi.seoultrip.fragment.FavoriteListFragment;
import tk.twpooi.seoultrip.fragment.MyCourseListFragment;
import tk.twpooi.seoultrip.fragment.RecommendCourseFragment;

/**
 * Created by tw on 2016-08-15.
 */
public class MainTabActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // GuillotineAnimation
    GuillotineAnimation mGuilotineAnimation;
    private boolean guillotineMenuIsOpened = false;

    // UI
    private CustomViewPager viewPager;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TextView toolbarBtn;
    private NavigationAdapter mPagerAdapter;
    private String[] toolbarTitleList = new String[5];//{"즐겨찾기", "명소 목록", "코스 추천", "나만의 코스", "코스 게임"};
    private static final int startPage = 2;

    // Daum map
    public static boolean isMapViewLoad = false;

    // Facebook
    private CallbackManager callbackManager;
    private String facebook_id = "";
    private String full_name = "";



    public static void setIsMapViewLoad(boolean f){
        isMapViewLoad = f;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main_tab);

        toolbarTitleList[0] = getResources().getString(R.string.favorite);
        toolbarTitleList[1] = getResources().getString(R.string.attraction_list);
        toolbarTitleList[2] = getResources().getString(R.string.recommend_course);
        toolbarTitleList[3] = getResources().getString(R.string.my_course);
        toolbarTitleList[4] = getResources().getString(R.string.course_game);

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();
        StartActivity.DATABASE_LANGUAGE = setting.getString("db_lang", "en");
        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        initUI(); // set tabbar
        setGuilotineUI();

        checkDangerousPermissions();
//        createMap();

    }

    @Override
    public void onBackPressed() {

        if (!guillotineMenuIsOpened) {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getResources().getString(R.string.ok))
                    .setContentText(getResources().getString(R.string.are_you_finish))
                    .setCancelText(getResources().getString(R.string.cancel))
                    .setConfirmText(getResources().getString(R.string.finish))
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            //super.onBackPressed();
                            sDialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    })
                    .show();


        }else {
            mGuilotineAnimation.close();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                if(!guillotineMenuIsOpened) {
                    mGuilotineAnimation.open();
                }
                return true;
        }

        return super.onKeyDown(keycode, e);
    }

    public void setViewPagerScrollEnabled(boolean enabled){
        viewPager.setPagingEnabled(enabled);
    }

    public Toolbar getToolbar(){
        return this.toolbar;
    }
    public void hideToolbar() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    public void showToolbar() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    private View guillotineMenu;

    private void setGuilotineUI(){
        // Add GuillotineMenu
        guillotineMenu = LayoutInflater.from(this).inflate(R.layout.support_main_tab, null);
        FrameLayout root = (FrameLayout)findViewById(R.id.main_activity_root);
        ImageView contentHamburger = (ImageView)findViewById(R.id.content_hamburger);

        // General Layout
        TextView versionText = (TextView)guillotineMenu.findViewById(R.id.version_text);
        versionText.setText(getResources().getString(R.string.app_name) + " " + getVersion() + " (" + getVersionCode() + ")");
        versionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences setting = getSharedPreferences("setting", 0);
//                SharedPreferences.Editor editor = setting.edit();
//                editor.clear();
//                editor.remove("DBVersion");
//                editor.commit();
//                showSnackbar("데이터베이스 버전 삭제 완료");
            }
        });

        TextView ossBtn = (TextView)guillotineMenu.findViewById(R.id.oss_btn);
        ossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OSSActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "oss");
                startActivity(intent);
            }
        });
        TextView dataBtn = (TextView)guillotineMenu.findViewById(R.id.data_btn);
        dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OSSActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "data");
                startActivity(intent);
            }
        });

        TextView changeDBLang = (TextView)guillotineMenu.findViewById(R.id.change_database_lang);
        changeDBLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDBLanguageDialog();
            }
        });

        setGuillotineMenuIsOpened();

        root.addView(guillotineMenu);

        mGuilotineAnimation = new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(250)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .setGuillotineListener(new GuillotineListener() {
                    @Override
                    public void onGuillotineOpened() {
                        setGuillotineMenuIsOpened();
                        guillotineMenuIsOpened = true;
                    }

                    @Override
                    public void onGuillotineClosed() {
                        guillotineMenuIsOpened = false;
                    }
                })
                .build();
    }

    private void setGuillotineMenuIsOpened(){

        RelativeLayout rl_fb_info = (RelativeLayout)guillotineMenu.findViewById(R.id.rl_fb_info);
        RelativeLayout rl_fb_info_none = (RelativeLayout)guillotineMenu.findViewById(R.id.rl_fb_info_none);

        Profile profile = Profile.getCurrentProfile();
        if(profile != null){

            rl_fb_info.setVisibility(View.VISIBLE);
            rl_fb_info_none.setVisibility(View.GONE);

            ImageView imgProfile = (ImageView)guillotineMenu.findViewById(R.id.img_profile);
            TextView name = (TextView)guillotineMenu.findViewById(R.id.name);

            ImageView fbLogout = (ImageView)guillotineMenu.findViewById(R.id.fb_logout);
            if(Locale.getDefault().getLanguage().equals("ko")){
                fbLogout.setImageResource(R.drawable.ic_facebook_logout_ko);
            }else{
                fbLogout.setImageResource(R.drawable.ic_facebook_logout_en);
            }
            fbLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logOut();
                    setGuillotineMenuIsOpened();
                }
            });

            facebook_id=profile.getId();
            full_name=profile.getName();
            profile.getProfilePictureUri(500, 500);
            Picasso.with(getApplicationContext())
                    .load(profile.getProfilePictureUri(250, 250))
                    .transform(new CropCircleTransformation())
                    .into(imgProfile);
            name.setText(full_name + getResources().getText(R.string.nim));
        }else{

            rl_fb_info.setVisibility(View.INVISIBLE);
            rl_fb_info_none.setVisibility(View.VISIBLE);


            ImageView loginButton = (ImageView)guillotineMenu.findViewById(R.id.login_button);
            if(Locale.getDefault().getLanguage().equals("ko")){
                loginButton.setImageResource(R.drawable.ic_login_facebook_ko);
            }else{
                loginButton.setImageResource(R.drawable.ic_login_facebook_en);
            }
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginFacebook();
                }
            });

        }


        // 여기부터 사용자 정보
        FileManager fileManager = new FileManager(getApplicationContext());

        SharedPreferences setting = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = setting.edit();
        int successCount = setting.getInt("successCourseGameCount", 0);

        TextView favoriteInfo = (TextView)guillotineMenu.findViewById(R.id.favorite_info);
        favoriteInfo.setText(String.format(getResources().getString(R.string.a_ea), fileManager.readFavorite().size()));
        TextView myCourseInfo = (TextView)guillotineMenu.findViewById(R.id.my_course_info);
        myCourseInfo.setText(String.format(getResources().getString(R.string.a_ea), fileManager.readMyCourse().size()));
        TextView shareCourseInfo = (TextView)guillotineMenu.findViewById(R.id.share_course_info);
        shareCourseInfo.setText(String.format(getResources().getString(R.string.a_times), fileManager.readSharedFile().size()));
        TextView courseGameInfo = (TextView)guillotineMenu.findViewById(R.id.course_game_info);
        courseGameInfo.setText(String.format(getResources().getString(R.string.a_times), successCount));


    }

    private void initUI() {

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarTitleList[startPage]);
        toolbarBtn = (TextView)findViewById(R.id.toolbar_btn);
        toolbarBtn.setVisibility(View.GONE);

        viewPager = (CustomViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setOffscreenPageLimit(toolbarTitleList.length);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        navigationTabBar.setTitleSize(30);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_star_outline_black_48dp),
                        Color.parseColor(colors[0]))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_image_area_black_48dp),
                        Color.parseColor(colors[1]))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_cards_playing_outline_black_48dp),
                        Color.parseColor(colors[2]))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_cards_outline_black_48dp),
                        Color.parseColor(colors[3]))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_flag_outline_variant_black_48dp),
                        Color.parseColor(colors[4]))
                        .build()
        );


        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, startPage);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
                switch (position){
                    case 0: {
                        FavoriteListFragment f = (FavoriteListFragment)mPagerAdapter.getItemAt(position);
                        f.refreshFragment();
                        toolbarBtn.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        AttractionListFragment f = (AttractionListFragment)mPagerAdapter.getItemAt(position);
                        f.refreshList();
                        break;
                    }
                    default:
                        toolbarBtn.setVisibility(View.GONE);
                        break;
                }
                toolbarTitle.setText(toolbarTitleList[position]);

            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                showToolbar();
            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }

    private void showWarningDialog(final String myLanguage){
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getResources().getString(R.string.warning))
                .setContentText(getResources().getString(R.string.are_you_change_language))
                .setCancelText(getResources().getString(R.string.no))
                .setConfirmText(getResources().getString(R.string.yes))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();

                        editor.putString("db_lang", myLanguage);
                        editor.commit();

                        Intent mStartActivity = new Intent(getApplicationContext(), StartActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, mPendingIntent);
                        System.exit(0);
                    }
                })
                .show();
    }

    private void ChangeDBLanguageDialog() {
        final String[] stringItems = {"한국어", "English"};
        final ActionSheetDialog dialog = new ActionSheetDialog(this, stringItems, null);
        dialog.title(getResources().getString(R.string.init_select_language))//
                .titleTextSize_SP(14.5f)//
                .cancelText(getResources().getString(R.string.cancel))
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                String lang = stringItems[position];

                switch (lang){
                    case "한국어":
                        showWarningDialog("ko");
                        break;
                    case "English":
                        showWarningDialog("en");
                        break;
                    default:
                        break;
                }

                dialog.dismiss();
            }
        });
    }

    // Tab Adapter Class (used initUI())
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter{

        public NavigationAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %5;
            switch (pattern){
                case 0:{
                    f = new FavoriteListFragment();
                    break;
                }
                case 1:{
                    f = new AttractionListFragment();
                    break;
                }
                case 2:{
                    f = new RecommendCourseFragment();
                    break;
                }
                case 3:{
                    f = new MyCourseListFragment();
                    break;
                }
                default:{
                    f = new CourseGameFragment();
                    break;
                }
            }
            return f;
        }

        @Override
        public int getCount(){
            return 5;
        }

    }

    private void loginFacebook(){

        LoginManager.getInstance().logInWithReadPermissions(MainTabActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showSnackbar(getResources().getString(R.string.success_login) + "!");
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    facebook_id=profile.getId();
                    full_name=profile.getName();
                }

                setGuillotineMenuIsOpened();

                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String profile_name=object.getString("name");
                                    long fb_id=object.getLong("id"); //use this for logout
                                    //Start new activity or use this info in your project.
                                    //Go another activity
                                } catch (JSONException e) {
                                    //  e.printStackTrace();
                                }

                            }

                        });

                request.executeAsync();
            }

            @Override
            public void onCancel() {
                showSnackbar(getResources().getString(R.string.cancel_login));
//                Toast.makeText(getApplicationContext(), "로그인 취소", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                showSnackbar(getResources().getString(R.string.error_login) + "\n" + error.toString());
            }
        });

    }


    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

        } else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                //Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String getVersionCode(){
        String version = "";
        try{
            PackageInfo i = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = Integer.toString(i.versionCode);
        }catch(PackageManager.NameNotFoundException e){
        }
        return version;
    }

    private String getVersion(){
        String version = "";
        try{
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception e){

        }
        return version;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
    }


}
