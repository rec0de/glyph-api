package com.ubercab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ubercab.databinding.FragmentFirstBinding;

public class GlyphAnimatorControlPage extends Fragment {

    private FragmentFirstBinding binding;

    private final String TAG = "GlyphAnimatorControlPage";

    private final int MAX_MINS_LEFT = 10;
    private int minsLeft = MAX_MINS_LEFT;
    private boolean isProgressInit = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonBreathingAnim.setOnClickListener(view1 -> {
            GlyphAnimator.GetInstance().TriggerBreathingAnim();

//            Properties props = System.getProperties();
//            Enumeration<Object> keys = props.keys();
//            while (keys.hasMoreElements()) {
//                String key = (String)keys.nextElement();
//                Log.d(TAG, key + ": " + props.get(key));
//            }

//            String buildType = System.getProperty("ro.build.version.type");
//            Snackbar snackbar = Snackbar.make(view, buildType, Snackbar.LENGTH_LONG);
//            snackbar.show();
        });

        binding.buttonFlashAnim.setOnClickListener(view1 -> {
            GlyphAnimator.GetInstance().TriggerFlashAnim();
        });

        binding.buttonTimeLeftDecrement.setOnClickListener(view1 -> {
            if (isProgressInit) {
                minsLeft--;
            }
            isProgressInit = true;
//            TriggerProgressAnim();
        });

        binding.buttonTimeLeftIncrement.setOnClickListener(view1 -> {
            minsLeft++;
//            TriggerProgressAnim();
        });

        binding.buttonCancel.setOnClickListener(view1 -> {
            GlyphAnimator.GetInstance().TurnOffRunningAnim();
            minsLeft = MAX_MINS_LEFT;
            isProgressInit = false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}