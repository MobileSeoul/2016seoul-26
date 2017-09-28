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


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.SimilarAttractionActivity;


public class FlexibleSpaceWithImageScrollViewFragment extends FlexibleSpaceWithImageBaseFragment<ObservableScrollView> {

    //MapView mapView = null;

    private View view;
    private DetailActivity activity;
    private Context context;

    private ArrayList<String> categorize = new ArrayList<>();
    private ArrayList<HashMap<String, String>> attractionList;
    private String language;

    // Database
    private DBHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_flexiblespacewithimagescrollview, container, false);
        context = container.getContext();

        activity = (DetailActivity)getActivity();
        language = activity.getDbLanguage();

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        final ObservableScrollView scrollView = (ObservableScrollView) view.findViewById(R.id.scroll);
        // TouchInterceptionViewGroup should be a parent view other than ViewPager.
        // This is a workaround for the issue #117:
        // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
        scrollView.setTouchInterceptionViewGroup((ViewGroup) view.findViewById(R.id.fragment_root));

        // Scroll to the specified offset after layout
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SCROLL_Y)) {
            final int scrollY = args.getInt(ARG_SCROLL_Y, 0);
            ScrollUtils.addOnGlobalLayoutListener(scrollView, new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, scrollY);
                }
            });
            updateFlexibleSpace(scrollY, view);
        } else {
            updateFlexibleSpace(0, view);
        }

        scrollView.setScrollViewCallbacks(this);

        String contents = activity.getContents();


        TextView detailContents = (TextView)view.findViewById(R.id.detailContents);
        detailContents.setText(contents);

        categorize = activity.getCategorize();

        String title = activity.getAttractionTitle();

        attractionList = dbHelper.getResultAllByCategorize(categorize, activity.getAttractionTitle(), StartActivity.DATABASE_LANGUAGE);

        TextView showSimilar = (TextView)view.findViewById(R.id.show_similar_attraction);
        showSimilar.setText(getResources().getString(R.string.look_similar_attraction) + " >>");
        showSimilar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long seed = System.nanoTime();
                Collections.shuffle(attractionList, new Random(seed));
                Intent intent = new Intent(context, SimilarAttractionActivity.class);
                intent.putExtra("attraction", attractionList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        if(attractionList.size() == 0){
            showSimilar.setVisibility(View.GONE);
        }

        return view;
    }


    @Override
    public void updateFlexibleSpace(int scrollY) {
        // Sometimes scrollable.getCurrentScrollY() and the real scrollY has different values.
        // As a workaround, we should call scrollVerticallyTo() to make sure that they match.
        Scrollable s = getScrollable();
        s.scrollVerticallyTo(scrollY);

        // If scrollable.getCurrentScrollY() and the real scrollY has the same values,
        // calling scrollVerticallyTo() won't invoke scroll (or onScrollChanged()), so we call it here.
        // Calling this twice is not a problem as long as updateFlexibleSpace(int, View) has idempotence.
        updateFlexibleSpace(scrollY, getView());
    }

    @Override
    protected void updateFlexibleSpace(int scrollY, View view) {
        ObservableScrollView scrollView = (ObservableScrollView) view.findViewById(R.id.scroll);

        // Also pass this event to parent Activity
        DetailActivity parentActivity =
                (DetailActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.onScrollChanged(scrollY, scrollView);
        }
    }
}
