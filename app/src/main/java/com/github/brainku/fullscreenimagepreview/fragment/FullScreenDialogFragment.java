package com.github.brainku.fullscreenimagepreview.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.github.brainku.fullscreenimagepreview.R;
import com.github.brainku.fullscreenimagepreview.adapter.SimpleViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

/**
 * FullScreenDialogFragment
 * Created by brainku on 17/8/26.
 */

public class FullScreenDialogFragment extends DialogFragment {

    private static final String KEY_POS = "POS";
    private static final String KEY_CONTENTS = "CONTENTS";

    private static final String KEY_LOCATION_X = "LOCATION_X";
    private static final String KEY_LOCATION_Y = "LOCATION_Y";
    private static final String KEY_WIDTH = "WIDTH";
    private static final String KEY_HEIGHT = "HEIGHT";
    private static final String KEY_IMAGE_BUNDLE = "IMAGE_BUNDLE";
    @BindView(R.id.view_pager_details)
    ViewPager viewPagerDetails;
    @BindView(R.id.img_placeholder)
    ImageView imgPlaceholder;
    Unbinder unbinder;

    public static FullScreenDialogFragment newInstance(List<Integer> covers, int pos, ImageView imageView) {
        Bundle args = new Bundle();
        args.putIntegerArrayList(KEY_CONTENTS, new ArrayList<>(covers));
        args.putInt(KEY_POS, pos);
        args.putBundle(KEY_IMAGE_BUNDLE, captureValue(imageView));
        FullScreenDialogFragment fragment = new FullScreenDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String KEY_SCALE = "SCALE";

    private static Bundle captureValue(ImageView view) {
        Bundle bundle = new Bundle();
        int[] location = new int[2];
        view.getLocationInWindow(location);
        bundle.putInt(KEY_WIDTH, view.getWidth());
        bundle.putInt(KEY_HEIGHT, view.getHeight());
        bundle.putInt(KEY_LOCATION_X, location[0]);
        bundle.putInt(KEY_LOCATION_Y, location[1]);
        bundle.putFloat(KEY_SCALE, scale(view));
        return bundle;
    }

    public static float scale(ImageView view) {
        float[] matrix = new float[9];
        view.getImageMatrix().getValues(matrix);
        Log.d(TAG, "matrix: " + Arrays.toString(matrix));
        return matrix[Matrix.MSCALE_X];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_photos_viewpager, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private DetailsAdapter mAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initArgument();
        mAdapter = new DetailsAdapter();
        mAdapter.setOnImageClick(new SimpleViewPagerAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(ImageView imgView, List<Integer> covers, int position) {
                dismiss();
            }
        });
        imgPlaceholder.setImageResource(mCovers.get(pos));
        viewPagerDetails.setAdapter(mAdapter);
        viewPagerDetails.setCurrentItem(pos);
        viewPagerDetails.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                imgPlaceholder.setImageResource(mCovers.get(position));
                if (mCallback != null) {
                    mCallback.changeTo(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imgPlaceholder.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgPlaceholder.getViewTreeObserver().removeOnPreDrawListener(this);
                prepareScene();
                runEnterAnimation();
                return true;
            }
        });
    }

    Bundle mEndValues;

    private void prepareScene() {
        mEndValues = captureValue(imgPlaceholder);
        Log.d(TAG, "prepareScene: " + mEndValues);
    }

    private void runEnterAnimation() {
        int startWidth = mStartValues.getInt(KEY_WIDTH);
        int startHeight = mStartValues.getInt(KEY_HEIGHT);
        int startPosX = mStartValues.getInt(KEY_LOCATION_X);
        int startPosY = mStartValues.getInt(KEY_LOCATION_Y);
        int endWidth = mEndValues.getInt(KEY_WIDTH);
        int endHeight = mEndValues.getInt(KEY_HEIGHT);
        int endPosX = mEndValues.getInt(KEY_LOCATION_X);
        int endPosY = mEndValues.getInt(KEY_LOCATION_Y);
        imgPlaceholder.setVisibility(View.VISIBLE);
        viewPagerDetails.setVisibility(View.GONE);
        // 这里会导致复原--因为起点就是原缩放方式。现在得让这里的起点跟原先的 View 一致
        float scale = mStartValues.getFloat(KEY_SCALE) / mEndValues.getFloat(KEY_SCALE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgPlaceholder, "scaleX", scale, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgPlaceholder, "scaleY", scale, 1);
//        ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(imgPlaceholder), startWidth, endWidth);
//        ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(imgPlaceholder), (int)(startHeight/scale), endHeight);
        ObjectAnimator translationX = ObjectAnimator.ofFloat(imgPlaceholder, "translationX", (startPosX - endPosX) / scale, 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(imgPlaceholder, "translationY", (startPosY - endPosY) / scale, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(scaleX, scaleY,/* widthAnimator,  heightAnimator,*/translationX, translationY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imgPlaceholder.setVisibility(View.INVISIBLE);
                viewPagerDetails.setVisibility(View.VISIBLE);
            }
        });
        animatorSet.start();
    }

    private void runExitAnimation(final Runnable runnable) {
        if (mCallback != null) {
            mStartValues = captureValue(mCallback.getCurrentImage(viewPagerDetails.getCurrentItem()));
            mEndValues = captureValue(imgPlaceholder);
        }
        int startWidth = mStartValues.getInt(KEY_WIDTH);
        int startHeight = mStartValues.getInt(KEY_HEIGHT);
        int startPosX = mStartValues.getInt(KEY_LOCATION_X);
        int startPosY = mStartValues.getInt(KEY_LOCATION_Y);
        int endWidth = mEndValues.getInt(KEY_WIDTH);
        int endHeight = mEndValues.getInt(KEY_HEIGHT);
        int endPosX = mEndValues.getInt(KEY_LOCATION_X);
        int endPosY = mEndValues.getInt(KEY_LOCATION_Y);
        imgPlaceholder.setVisibility(View.VISIBLE);
        viewPagerDetails.setVisibility(View.GONE);
        float scale = mStartValues.getFloat(KEY_SCALE) / mEndValues.getFloat(KEY_SCALE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgPlaceholder, "scaleX", 1, scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgPlaceholder, "scaleY", 1, scale);
//        ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(imgPlaceholder), startWidth, endWidth);
//        ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(imgPlaceholder), (int)(startHeight/scale), endHeight);
        ObjectAnimator translationX = ObjectAnimator.ofFloat(imgPlaceholder, "translationX", 0, (startPosX - endPosX) / scale);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(imgPlaceholder, "translationY", 0, (startPosY - endPosY) / scale);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(scaleX, scaleY/* , widthAnimator,  heightAnimator*/,translationX, translationY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                imgPlaceholder.setVisibility(View.VISIBLE);
                viewPagerDetails.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                runnable.run();
            }
        });
        animatorSet.start();
    }

    @Override
    public void dismiss() {
        runExitAnimation(new Runnable() {
            @Override
            public void run() {
                FullScreenDialogFragment.super.dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (OnPageChangeCallback) context;
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    List<Integer> mCovers;
    int pos;
    Bundle mStartValues;
    private void initArgument() {
        Bundle args = getArguments();
        mCovers = args.getIntegerArrayList(KEY_CONTENTS);
        pos = args.getInt(KEY_POS);
        mStartValues = args.getBundle(KEY_IMAGE_BUNDLE);
        Log.d(TAG, "initArgument: " + mStartValues);
    }

    private static final String TAG = "TAG_ACT";

    private class DetailsAdapter extends SimpleViewPagerAdapter {

        @Override
        public ImageView createImage(ViewGroup container, int position) {
            ImageView imageView = new ImageView(container.getContext());
            imageView.setScaleType(CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
            return imageView;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), R.style.FullScreenDialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private static class WidthEvaluator extends IntEvaluator {

        private View v;
        public WidthEvaluator(View v) {
            this.v = v;
        }

        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            int num = super.evaluate(fraction, startValue, endValue);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.width = num;
            v.setLayoutParams(params);
            return num;
        }
    }

    private static class HeightEvaluator extends IntEvaluator {

        private View v;
        public HeightEvaluator(View v) {
            this.v = v;
        }

        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            int num = super.evaluate(fraction, startValue, endValue);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = num;
            v.setLayoutParams(params);
            return num;
        }
    }

    OnPageChangeCallback mCallback;

    public interface OnPageChangeCallback {
        void changeTo(int position);
        ImageView getCurrentImage(int position);
    }
}
