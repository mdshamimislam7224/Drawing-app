package com.mdshamimislam.drawing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;
import com.mdshamimislam.drawing.R;
import com.mdshamimislam.drawing.activity.Drawing;
import com.mdshamimislam.drawing.activity.MainActivity;
import com.mdshamimislam.drawing.adapter.ViewAdapter;
import com.mdshamimislam.drawing.interfaces.OnClick;
import com.mdshamimislam.drawing.util.Constant;
import com.mdshamimislam.drawing.util.Method;


public class HomeFragment extends Fragment {

    private int[] image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.home));
        }

        OnClick onClick = (position, type) -> {
            Constant.image = image;
            startActivity(new Intent(getActivity(), Drawing.class)
                    .putExtra("position", position));
        };
        Method method = new Method(getActivity(), onClick);

        image = new int[]{R.drawable.image, R.drawable.image_a,R.drawable.lion_lion,R.drawable.bird,R.drawable.chingri,R.drawable.cup
        ,R.drawable.duck,R.drawable.jog,R.drawable.village_sciene};

        MaterialTextView textViewNoData = view.findViewById(R.id.textView_noData_home);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_fragment);

        textViewNoData.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        ViewAdapter viewAdapter = new ViewAdapter(getActivity(), image, method);
        recyclerView.setAdapter(viewAdapter);

        return view;

    }

}
