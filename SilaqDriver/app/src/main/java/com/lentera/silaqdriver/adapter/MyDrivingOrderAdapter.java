package com.lentera.silaqdriver.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.strictmode.UnbufferedIoViolation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.lentera.silaqdriver.DrivingActivity;
import com.lentera.silaqdriver.R;
import com.lentera.silaqdriver.common.Common;
import com.lentera.silaqdriver.model.DrivingOrderModel;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class MyDrivingOrderAdapter extends RecyclerView.Adapter<MyDrivingOrderAdapter.MyViewHolder> {

    Context context;
    List<DrivingOrderModel> drivingOrderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyDrivingOrderAdapter(Context context, List<DrivingOrderModel> drivingOrderModelList) {
        this.context = context;
        this.drivingOrderModelList = drivingOrderModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Paper.init(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_order_driver,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(drivingOrderModelList.get(position).getOrderModel().getCartItemList()
                .get(0).getFoodImage())
                .into(holder.img_food);

        holder.txt_date.setText(new StringBuilder(
                simpleDateFormat.format(drivingOrderModelList.get(position).getOrderModel().getCreateDate())
        ));

        Common.setSpanStringColor("No. : ",
                drivingOrderModelList.get(position).getOrderModel().getKey(),
                holder.txt_order_number, Color.parseColor("#BA454A"));

        Common.setSpanStringColor("Alamat : ",
                drivingOrderModelList.get(position).getOrderModel().getShippingAddress(),
                holder.txt_order_address, Color.parseColor("#BA454A"));

        Common.setSpanStringColor("Pembayaran: ",
                drivingOrderModelList.get(position).getOrderModel().getTransactionId(),
                holder.txt_payment, Color.parseColor("#BA454A"));


        Common.setSpanStringColor("Total Harga:",
                String.valueOf(drivingOrderModelList.get(position).getOrderModel().getTotalPayment()),holder.total,Color.parseColor("#1B1C1E"));

        Common.setSpanStringColor("Toko: ",
                drivingOrderModelList.get(position).getOrderModel().getMarket(),
                holder.txt_market,Color.parseColor("#BA454A"));

        //matikan tombol
        if (drivingOrderModelList.get(position).isStartTrip()){
            holder.btn_drive_now.setEnabled(false);
        }

        //event
        holder.btn_drive_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Paper.book().write(Common.DRIVING_ORDER_DATA, new Gson().toJson(drivingOrderModelList.get(position)));
            context.startActivity(new Intent(context, DrivingActivity.class));
            }
        });


    }

    @Override
    public int getItemCount() {
        return drivingOrderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        @BindView(R.id.txt_date)
        TextView txt_date;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_payment)
        TextView txt_payment;
        @BindView(R.id.txt_market)
        TextView txt_market;
        @BindView(R.id.txt_total)
        TextView total;
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.btn_drive_now)
        MaterialButton btn_drive_now;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
