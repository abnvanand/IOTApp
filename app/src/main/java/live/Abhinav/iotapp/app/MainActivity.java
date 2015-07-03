package live.Abhinav.iotapp.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements AdapterProducts.ClickListener {

    private AppController volleySingleton;
    private RequestQueue requestQueue;

    //Array list of POJO
    private ArrayList<Product> productArrayList = new ArrayList<Product>();

    //Get a reference to the adapter to be passed to setAdapter() method
    private AdapterProducts adapterProducts;
    //Recycler view
    private RecyclerView recyclerView;


    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        volleySingleton = AppController.getInstance();
        requestQueue = volleySingleton.getRequestQueue();


        recyclerView = (RecyclerView) findViewById(R.id.listTransactions);

        adapterProducts = new AdapterProducts(getApplicationContext());
        adapterProducts.setClickListener(this);
        recyclerView.setAdapter(adapterProducts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        /*productArrayList.add(new Product(false,"1","Apple"));
        productArrayList.add(new Product(false,"1","Banana"));
        productArrayList.add(new Product(true, "1", "Potato"));
        productArrayList.add(new Product(false, "1", "Petrol"));
        productArrayList.add(new Product(false, "1", "Nutrela"));
        adapterProducts.setProductArrayList(productArrayList);*/

        sendJsonRequest();

    }

    private void sendJsonRequest() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                "http://192.168.2.16/volley/list.json",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Lifecycle3", response.toString());
                        productArrayList = parseJSONResponse(response);
                        adapterProducts.setProductArrayList(productArrayList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("Lifecycle4",volleyError.toString());
                    }
                });
        requestQueue.add(request);
    }

    private ArrayList<Product> parseJSONResponse(JSONArray response) {
        ArrayList<Product> listTransactions = new ArrayList<Product>();
        if (response != null && response.length() > 0) {
            try {
                StringBuilder data = new StringBuilder();
                JSONArray jsonArray = response;
                Log.d("Lifecycle1", String.valueOf(response.length()));
                for (int i = 0; i < jsonArray.length(); i++) {
                    Log.d("Lifecycle", "Inside loop");

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String tDate = jsonObject.getString("price");
                    String tOtherPartyName = jsonObject.getString("name");


                    data.append(tDate + " " + tOtherPartyName + " " + "\n");

                    Product product = new Product();
                    product.setIsChecked(false);
                    product.setpName(tOtherPartyName);
                    product.setpSNo(tDate);

                    listTransactions.add(product);
                    Log.d("Lifecycle", "Inside loop");

                }
                Log.d("Lifecycle2", listTransactions.toString());
            } catch (JSONException e) {
                Log.d("Lifecycle", "Inside JSON EXCEPTION: " + e);
            }
        }

        return listTransactions;
    }

    @Override
    public void itemClicked(View view, int position) {
        Toast.makeText(this, "Position "+position, Toast.LENGTH_SHORT).show();

    }
}