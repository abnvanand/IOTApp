package live.Abhinav.iotapp.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import net.sourceforge.zbar.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;

import static live.Abhinav.iotapp.app.Keys.*;


public class MainActivity extends Activity implements AdapterProducts.ClickListener, View.OnClickListener {

    /**
     * Bluetooth
     */
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();


    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    //-----------------------
    TextView tv_deviceAddress;
    TextView tv_deviceName;
    ArrayList<String> arrayListName = new ArrayList<String>();
    ArrayList<String> arrayListAddress = new ArrayList<String>();
    //-----------------------
    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 6000000;

    /**
     * Crouton
     */
    private static final Style INFINITE = new Style.Builder().
            setBackgroundColorValue(Style.holoBlueLight).build();
    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE)
            .build();
    private Crouton infiniteCrouton;

    ArrayList<String> croutonArrayList = new ArrayList<String>();
    //--------end of crouton------------


    //Camera-----------------------start
    FrameLayout cameraPreview;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;


    Button scanButton;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;


    //Camera-----------------------end

    private AppController volleySingleton;
    private RequestQueue requestQueue;

    //Array list of POJO
    private ArrayList<Product> productArrayList = new ArrayList<Product>();

    //Get a reference to the adapter to be passed to setAdapter() method
    private AdapterProducts adapterProducts;
    //Recycler view
    private RecyclerView recyclerView;


    FragmentManager fragmentManager;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        volleySingleton = AppController.getInstance();
        requestQueue = volleySingleton.getRequestQueue();


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        adapterProducts = new AdapterProducts(getApplicationContext());
        adapterProducts.setClickListener(this);
        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        cameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(barcodeScanned) {
                    barcodeScanned=false;

                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing=true;
                }
            }
        });
        recyclerView.setAdapter(adapterProducts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        /*productArrayList.add(new Product(false,"1","Apple"));
        productArrayList.add(new Product(false,"1","Banana"));
        productArrayList.add(new Product(true, "1", "Potato"));
        productArrayList.add(new Product(false, "1", "Petrol"));
        productArrayList.add(new Product(false, "1", "Nutrela"));
        adapterProducts.setProductArrayList(productArrayList);*/
//        prepareCamera();
        sendJsonRequest();

    }



    private void sendJsonRequest() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                "http://ecomxebia.esy.es/list.php",
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
                        Log.d("Lifecycle4", volleyError.toString());
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
                    String tSNo = jsonObject.getString(KEY_SNO);
                    String tProductName = jsonObject.getString(KEY_PNAME);

                    data.append(tSNo + " " + tProductName + " " + "\n");

                    Product product = new Product();
                    product.setIsChecked(false);
                    product.setpName(tProductName);
                    product.setpSNo(tSNo);

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
//        Toast.makeText(this, "Position " + position, Toast.LENGTH_SHORT).show();
        showCrouton("Position=" + position);
        recyclerView.scrollToPosition(0);
    }

    /**
     * Camera specific methods
     * -------------start-----------------
     */
    public void prepareCamera() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);

        cameraPreview.addView(mPreview);

    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
            cameraPreview.removeView(mPreview);
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    // scanText.setText("barcode result " + sym.getData());
                    Toast.makeText(getApplicationContext(), sym.getData(), Toast.LENGTH_SHORT).show();
                    adapterProducts.test("63000");
                    barcodeScanned = true;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
/**
 * Camera specific methods
 * -------------end-----------------
 */
/**
 * Bluetooth method
 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void prepareBluetooth() {
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!arrayListAddress.contains(device.getAddress())) {
                                mLeDevices.add(device);
                                arrayListName.add(device.getName() + " ");
                                arrayListAddress.add(device.getAddress() + " ");

                                Log.d("Lifecycle", device.getName() + " " + device.getAddress());
//                                if (arrayListAddress.size() > 0) {
                                String deviceAddress = arrayListAddress.get(arrayListAddress.size() - 1);
                                String deviceName = arrayListName.get(arrayListName.size() - 1);
                                Toast.makeText(MainActivity.this,
                                        device.getName() + " " + device.getAddress(),Toast.LENGTH_LONG).show();

//                                tv_deviceName.setText(deviceName);
//                                tv_deviceAddress.setText(deviceAddress);
//                                }
                            }
//                    mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    /**
     * Crouton COde
     */
    private void showCrouton(String croutonText) {
        croutonArrayList.add(croutonText);
        Crouton.cancelAllCroutons();

        final Crouton crouton;
        crouton = Crouton.makeText(this, croutonArrayList.get(croutonArrayList.size() - 1), INFINITE);
//        infiniteCrouton = crouton;
        crouton.setOnClickListener(this).setConfiguration(CONFIGURATION_INFINITE).show();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Item Closed", Toast.LENGTH_LONG).show();
        removeCrouton();
    }

    public void removeCrouton() {

        if (croutonArrayList.size() > 0) {
            croutonArrayList.remove(croutonArrayList.size() - 1);
            Crouton.cancelAllCroutons();
            if (croutonArrayList.size() > 0) {
                Crouton crouton;
                crouton = Crouton.makeText(this, croutonArrayList.get(croutonArrayList.size() - 1), INFINITE);
                crouton.setOnClickListener(this).setConfiguration(CONFIGURATION_INFINITE).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Anand", "onDestroy");

        Crouton.cancelAllCroutons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Anand", "onResume");
        prepareCamera();
        prepareBluetooth();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        scanLeDevice(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Anand", "onPause");
        releaseCamera();
        scanLeDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}