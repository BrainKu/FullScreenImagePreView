package com.github.brainku.fullscreenimagepreview.adapter;

import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.brainku.fullscreenimagepreview.R;

import java.util.Arrays;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by brainku on 17/8/26.
 */
public class SimpleViewPagerAdapter extends PagerAdapter {

    public SparseArray<ImageView> imageViewSparseArray = new SparseArray<>();

    public List<Integer> mCovers = Arrays.asList(R.mipmap.pic_1, R.mipmap.pic_2,R.mipmap.pic_3,R.mipmap.pic_4);

    @Override
    public int getCount() {
        return mCovers.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final ImageView imageView = createImage(container, position);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        container.addView(imageView, params);
        imageView.setImageResource(mCovers.get(position));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onImageClick(imageView, mCovers, position);
                }
            }
        });
        imageViewSparseArray.put(position, imageView);
        return imageView;
    }

    public ImageView createImage(ViewGroup container, int position) {
        final ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    public ImageView getCurrentImageView(int position) {
        return imageViewSparseArray.get(position);
    }

    OnImageClickListener mListener;

    public void setOnImageClick(OnImageClickListener listener) {
        mListener = listener;
    }

    public interface OnImageClickListener {
        void onImageClick(ImageView imgView, List<Integer> covers, int position);

    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        imageViewSparseArray.remove(position);
        container.removeView((View) object);
    }
}
