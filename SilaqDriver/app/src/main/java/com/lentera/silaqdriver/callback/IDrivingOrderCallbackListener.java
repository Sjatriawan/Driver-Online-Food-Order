package com.lentera.silaqdriver.callback;

import com.lentera.silaqdriver.model.DrivingOrderModel;

import java.util.List;

public interface IDrivingOrderCallbackListener {
    void onDrivingOrderLoadSuccess(List<DrivingOrderModel> drivingOrderModelList);
    void onDrivingOrderLoadFailed (String message);
}
