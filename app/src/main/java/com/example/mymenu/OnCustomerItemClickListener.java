package com.example.mymenu;

import android.view.View;

public interface OnCustomerItemClickListener {
    public void onItemClick(CustomerAdapter.ViewHolder holder, View view, int position);
}
