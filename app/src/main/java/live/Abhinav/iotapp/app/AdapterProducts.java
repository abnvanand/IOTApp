package live.Abhinav.iotapp.app;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Abhinav on 7/3/2015.
 */
public class AdapterProducts extends RecyclerView.Adapter<AdapterProducts.ViewHolderProducts> {
    private ArrayList<Product> productArrayList = new ArrayList<Product>();
    private LayoutInflater layoutInflater;
    private AppController volleySingleton;
    private ImageLoader imageLoader;
    Context context;

    private ClickListener clickListener;


    public AdapterProducts(Context context) {
        layoutInflater = LayoutInflater.from(context);
        volleySingleton = AppController.getInstance();
        this.context = context;
    }

    public void setProductArrayList(ArrayList<Product> arrayList) {
        this.productArrayList = arrayList;
        notifyItemRangeChanged(0, arrayList.size());
    }

    public void test(String barcodeString) {
        for (int i = 0; i < this.productArrayList.size(); i++) {
            if (productArrayList.get(i).getpSNo().equals(barcodeString)) {
                Toast.makeText(context, "Inside if", Toast.LENGTH_LONG).show();
                Product p=productArrayList.remove(i);
                p.setIsChecked(true);
                productArrayList.add(p);
                notifyItemRangeChanged(0, productArrayList.size());
            }
        }
    }

    public ArrayList<Product> getProductArrayList() {
        return productArrayList;
    }

    @Override
    public ViewHolderProducts onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_single_row, parent, false);
        ViewHolderProducts viewHolder = new ViewHolderProducts(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderProducts holder, int position) {
        Product curentProduct = productArrayList.get(position);
        holder.checkBox.setChecked(curentProduct.isChecked());
        holder.tv_productName.setText(curentProduct.getpName());
        holder.tv_productSNo.setText(curentProduct.getpSNo());
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }


    class ViewHolderProducts extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CheckBox checkBox;
        private TextView tv_productName;
        private TextView tv_productSNo;

        public ViewHolderProducts(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.isChecked);
            checkBox.setEnabled(false);
            tv_productName = (TextView) itemView.findViewById(R.id.tv_productName);
            tv_productSNo = (TextView) itemView.findViewById(R.id.tv_productSNo);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();

//            context.startActivity(new Intent(context, SubActivity.class));
            if (clickListener != null) {
                clickListener.itemClicked(v, position);
            }
//            Toast.makeText(context, "Item clicked at: " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            Product currProduct = productArrayList.get(position);
            currProduct.setIsChecked(!currProduct.isChecked());
            checkBox.setChecked(currProduct.isChecked());
            if (currProduct.isChecked())
                moveToBottom(getLayoutPosition());
            else
                moveToTop(getLayoutPosition());
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
    }

    public void moveToBottom(int position) {
        productArrayList.add(productArrayList.remove(position));
        notifyItemMoved(position, productArrayList.size() - 1);
    }

    public void moveToTop(int position) {
        productArrayList.add(0, productArrayList.remove(position));
        notifyItemMoved(position, 0);
    }

    public boolean checkBarCode() {
//        productArrayList.indexOf(productArrayList.)
        return true;
    }
}