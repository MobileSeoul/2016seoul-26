/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.twpooi.seoultrip.fragment.detailattraction.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;

import tk.twpooi.seoultrip.CustomObservableListView;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.ShowMapActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;


public class FlexibleSpaceWithImageListViewFragment extends FlexibleSpaceWithImageBaseFragment<ObservableListView> {

    View view;
    private CustomObservableListView listView;
    private Context context;

    // Daum map
//    static public MapView mapView;

    private ArrayList<String[]> list;

    public View getFragmentView(){
        return view;
    }

    public ObservableListView getListView(){
        return listView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_flexiblespacewithimagelistview, container, false);
        context = container.getContext();

        DetailActivity activity = (DetailActivity)getActivity();

        listView = (CustomObservableListView) view.findViewById(R.id.scroll);
        // Set padding view for ListView. This is the flexible space.
        View paddingView = new View(getActivity());
        final int flexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                flexibleSpaceImageHeight);
        paddingView.setLayoutParams(lp);

        // This is required to disable header's list selector effect
        paddingView.setClickable(true);

        listView.addHeaderView(paddingView);

        CustomInformationAdapter m_Adapter = new CustomInformationAdapter(this);

        listView.setAdapter(m_Adapter);

        list = activity.getInformationList();
        if(list != null){

            for(int i=0; i<list.size(); i++){
                m_Adapter.add(list.get(i));
            }

        }else{
            setDummyData(listView);
        }

        // TouchInterceptionViewGroup should be a parent view other than ViewPager.
        // This is a workaround for the issue #117:
        // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
        listView.setTouchInterceptionViewGroup((ViewGroup) view.findViewById(R.id.fragment_root));

        // Scroll to the specified offset after layout
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SCROLL_Y)) {
            final int scrollY = args.getInt(ARG_SCROLL_Y, 0);
            ScrollUtils.addOnGlobalLayoutListener(listView, new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    int offset = scrollY % flexibleSpaceImageHeight;
                    listView.setSelectionFromTop(0, -offset);
                }
            });
            updateFlexibleSpace(scrollY, view);
        } else {
            updateFlexibleSpace(0, view);
        }

        listView.setScrollViewCallbacks(this);
        listView.setOnItemClickListener(onItemClickListener);

        updateFlexibleSpace(0, view);

        return view;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final int index = position-1;
            if(index >= 0 && index < list.size()) {

                String title = list.get(index)[0];
                String contents = list.get(index)[1];

                if(title.equals(getResources().getString(R.string.telephone))){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contents));
                    context.startActivity(intent);
                }else if(title.equals(getResources().getString(R.string.website)) || title.equals(getResources().getString(R.string.source))){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
                    context.startActivity(intent);
                }else if(title.equals(getResources().getString(R.string.address))){
                    if(!ShowMapActivity.isMapLoading) {
                        String attraction = ((DetailActivity) getActivity()).getAttractionTitle();
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add(attraction);

                        Intent intent = new Intent(context, ShowMapActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("attraction", temp);
                        intent.putExtra("language", DetailActivity.dbLanguage);
                        context.startActivity(intent);
                    }
                }else{
                    setClipBoardLink(context, contents);
                }
            }
        }
    };

    public void setClipBoardLink(Context context , String link){

        ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", link);
        clipboardManager.setPrimaryClip(clipData);
        showSnackbar(getResources().getString(R.string.save_clipboard));

    }
    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }


    public void setScrollEnabled(boolean b){
        listView.setPagingEnabled(b);
    }

    @SuppressWarnings("NewApi")
    @Override
    public void setScrollY(int scrollY, int threshold) {
        View view = getView();
        if (view == null) {
            return;
        }
        ObservableListView listView = (ObservableListView) view.findViewById(R.id.scroll);
        if (listView == null) {
            return;
        }
        View firstVisibleChild = listView.getChildAt(0);
        if (firstVisibleChild != null) {
            int offset = scrollY;
            int position = 0;
            if (threshold < scrollY) {
                int baseHeight = firstVisibleChild.getHeight();
                position = scrollY / baseHeight;
                offset = scrollY % baseHeight;
            }
            listView.setSelectionFromTop(position, -offset);
        }
    }

    @Override
    protected void updateFlexibleSpace(int scrollY, View view) {
        int flexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);

        View listBackgroundView = view.findViewById(R.id.list_background);

        // Translate list background
        ViewHelper.setTranslationY(listBackgroundView, Math.max(0, -scrollY + flexibleSpaceImageHeight));

        // Also pass this event to parent Activity
        DetailActivity parentActivity =
                (DetailActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.onScrollChanged(scrollY, (ObservableListView) view.findViewById(R.id.scroll));
        }
    }

}
