package com.github.brainku.fullscreenimagepreview;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.github.brainku.fullscreenimagepreview.adapter.SimpleViewPagerAdapter;
import com.github.brainku.fullscreenimagepreview.fragment.FullScreenDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements FullScreenDialogFragment.OnPageChangeCallback {

    @BindView(R.id.view_pager_test)
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        ButterKnife.bind(this);
        SimpleViewPagerAdapter mAdapter = new SimpleViewPagerAdapter();
        mPager.setAdapter(mAdapter);
        mAdapter.setOnImageClick(new SimpleViewPagerAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(ImageView imgView, List<Integer> covers, int position) {
                FullScreenDialogFragment.newInstance(covers, position, imgView).show(getFragmentManager(), "FullScreen");
            }
        });
    }

    @Override
    public void changeTo(int position) {
        mPager.setCurrentItem(position);
    }
}
