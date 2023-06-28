package com.example.androidbarberstaffapp.Adapter;

import android.content.Context;
import android.database.DatabaseUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.IOnShoppingItemSelected;
import com.example.androidbarberstaffapp.Common.Interface.IRecyclerItemSelectedListener;
import com.example.androidbarberstaffapp.Model.ShoppingItem;
import com.example.androidbarberstaffapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder>{

    Context context;
    List<ShoppingItem> shoppingItemList;
    IOnShoppingItemSelected iOnShoppingItemSelected;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList, IOnShoppingItemSelected iOnShoppingItemSelected) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
        this.iOnShoppingItemSelected = iOnShoppingItemSelected;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_shopping_item_name, txt_shopping_item_price, txt_add_to_cart;
        ImageView img_shopping_item;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img_shopping_item = (ImageView)itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = (TextView)itemView.findViewById(R.id.txt_name_shopping_item);
            txt_shopping_item_price = (TextView)itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_cart = (TextView)itemView.findViewById(R.id.txt_add_to_cart);

            txt_add_to_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyShoppingItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shopping_item, parent, false);
        return new MyShoppingItemAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyShoppingItemAdapter.MyViewHolder holder, int position) {
        Picasso.get().load(shoppingItemList.get(position).getImage()).into(holder.img_shopping_item);
        holder.txt_shopping_item_name.setText(Common.formatShoppingItemName(shoppingItemList.get(position).getName()));
        holder.txt_shopping_item_price.setText(new StringBuilder(shoppingItemList.get(position).getPrice().toString()).append(" VNĐ"));

        // Add to cart from Staff app
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                iOnShoppingItemSelected.onShoppingItemSelected(shoppingItemList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }
}