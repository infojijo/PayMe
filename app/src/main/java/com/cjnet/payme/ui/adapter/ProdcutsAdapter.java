package com.cjnet.payme.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.billingclient.api.SkuDetails;
import com.cjnet.payme.R;
import com.cjnet.payme.ui.home.HomeFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProdcutsAdapter extends RecyclerView.Adapter<ProdcutsAdapter.ProductViewHolder> {

    private List<SkuDetails> mProduct;
    private HomeFragment mContext;

    public ProdcutsAdapter(List<SkuDetails> mProduct, HomeFragment mContext) {
        this.mProduct = mProduct;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {
        holder.mTitleText.setText(mProduct.get(position).getTitle());
        holder.mTitleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startBilling(mProduct.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProduct.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleText;

        public ProductViewHolder(View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.text);
        }

    }

    public interface doBilling {

        void startBilling(SkuDetails skuID);
    }
}
