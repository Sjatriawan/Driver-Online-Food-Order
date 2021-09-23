package com.lentera.silaqdriver.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaqdriver.callback.IDrivingOrderCallbackListener;
import com.lentera.silaqdriver.common.Common;
import com.lentera.silaqdriver.model.DrivingOrderModel;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IDrivingOrderCallbackListener {

    private MutableLiveData<List<DrivingOrderModel>> drivingOrderMutableData;
    private MutableLiveData<String> messageError;
    private IDrivingOrderCallbackListener listener;

    public HomeViewModel() {
        drivingOrderMutableData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;

    }
    public MutableLiveData<List<DrivingOrderModel>> getDrivingOrderMutableData(String driverPhone) {
        loadOrderByDriver(driverPhone);
        return drivingOrderMutableData;
    }

    private void loadOrderByDriver(String driverPhone) {
        List<DrivingOrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.DRIVING_ORDER_REF)
                .orderByChild("driverPhone")
                .equalTo(Common.currentDriverUser.getPhone());
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnapshot:snapshot.getChildren()){
                    DrivingOrderModel drivingOrderModel = orderSnapshot.getValue(DrivingOrderModel.class);
                    tempList.add(drivingOrderModel);
                }
                listener.onDrivingOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDrivingOrderLoadFailed(error.getMessage());
            }
        });
    }
    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onDrivingOrderLoadSuccess(List<DrivingOrderModel> drivingOrderModelList) {
        drivingOrderMutableData.setValue(drivingOrderModelList);
    }

    @Override
    public void onDrivingOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}