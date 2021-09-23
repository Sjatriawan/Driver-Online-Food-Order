package com.lentera.silaqdriver.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lentera.silaqdriver.R;
import com.lentera.silaqdriver.adapter.MyDrivingOrderAdapter;
import com.lentera.silaqdriver.common.Common;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    @BindView(R.id.recycler_order)
    RecyclerView recyclerOrder;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;

    MyDrivingOrderAdapter adapter;

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(root);
        homeViewModel.getMessageError().observe(this,s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getDrivingOrderMutableData(Common.currentDriverUser.getPhone() ).observe(this, drivingOrderModel -> {
            adapter = new MyDrivingOrderAdapter(getContext(), drivingOrderModel);
            recyclerOrder.setAdapter(adapter);
            recyclerOrder.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initViews( View root) {
        unbinder = ButterKnife.bind(this,root);

        recyclerOrder.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerOrder.setLayoutManager(layoutManager);
        recyclerOrder.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_slide_from_left);

    }
}
