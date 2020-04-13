package com.example.htc20;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CitizenPurchaseActivity extends AppCompatActivity {

    private ProgressBar progress_bar;
    private Button add_order;
    private Button place_order;
    private TextView shop;
    private Button return_dashboard;
    private ScrollView scroll_view;
    private LinearLayout vertical_layout;

    private FirebaseFirestore db;
    private FirebaseAuth fbauth;


    private Double latitude;
    private Double longitude;
    private String shop_name;
    private String customer_details;
    private boolean is_database;
    private String shop_unique_id;

    private int number_of_orders = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_purchase);

        Bundle extras = getIntent().getExtras();
        String[] extraInfo = extras.getString("store_info").split("@", -1);
        latitude = Double.parseDouble(extraInfo[0]);
        longitude = Double.parseDouble(extraInfo[1]);
        shop_name = extraInfo[2];
        is_database = Boolean.parseBoolean(extraInfo[3]);
        shop_unique_id = extraInfo[4];

        shop = (TextView) findViewById(R.id.etShopNameAndID);
        return_dashboard = (Button) findViewById(R.id.btn_return);
        add_order = (Button) findViewById(R.id.etAddOrder);
        place_order = (Button) findViewById(R.id.etSendOrder);
        progress_bar = (ProgressBar) findViewById(R.id.add_product_pbar);
        scroll_view = (ScrollView) findViewById(R.id.etScrollView);
        vertical_layout = findViewById(R.id.etVerticalLayout);

        Log.d("ShopName", shop_name);
        shop.setText(shop_name);
        progress_bar.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        getCustomerDetails();

        place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = true;
                String order = "";
                int count = 0;
                for (int i = 1; i <= number_of_orders; i++) {
                    LinearLayout layout = (LinearLayout) vertical_layout.getChildAt(i);
                    AutoCompleteTextView item_name = (AutoCompleteTextView) layout.getChildAt(0);
                    AutoCompleteTextView item_quantity = (AutoCompleteTextView) layout.getChildAt(1);
                    if((item_name.getText().toString().trim().isEmpty() && !item_quantity.getText().toString().isEmpty()) || (!item_name.getText().toString().isEmpty() && item_quantity.getText().toString().isEmpty())){
                        result = false;
                        addItemsCorrectly();
                        break;
                    }
                    else if(!item_name.getText().toString().isEmpty() && !item_quantity.getText().toString().isEmpty()){
                        count++;
                        order += count + ". " + item_name.getText().toString().trim() + " X " + item_quantity.getText().toString().trim() + "|\n";
                    }
                }
                if(result == true && count >=1){
                    progress_bar.setVisibility(View.VISIBLE);
                    final Map<String, String> user = new HashMap<>();
                    user.put("unique_id", shop_unique_id);
                    user.put("shop_name", shop_name);
                    user.put("customer_details", customer_details);
                    user.put("order_size", String.valueOf(count));
                    user.put("order_placed", order);
                    db.collection("orders")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(CitizenPurchaseActivity.this, "Sent Order to Store", Toast.LENGTH_SHORT).show();
                                }
                            });
                    startActivity(new Intent(CitizenPurchaseActivity.this, DashboardCitizenActivity.class));
                }
                else{
                    Toast.makeText(CitizenPurchaseActivity.this, "Please enter the order correctly", Toast.LENGTH_SHORT).show();
                }
            }
        });

        add_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number_of_orders < 15){
                    updateOrderCount();
                    addNewOrder();
                }
                else{
                    cantAddMoreOrders();
                }
            }
        });

        return_dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CitizenPurchaseActivity.this, DashboardCitizenActivity.class));
            }
        });

    }

    private void updateUserInfo(String name, String phone_no){
        customer_details = name + "#" + phone_no;
    }

    private void getCustomerDetails(){
        fbauth = FirebaseAuth.getInstance();
        FirebaseUser user = fbauth.getCurrentUser();
        String email = user.getEmail();

        db.collection("citizen")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            String name = doc.getData().get("first_name") + " " + doc.getData().get("last_name");
                            String phone_no = String.valueOf(doc.getData().get("phone_number"));
                            updateUserInfo(name, phone_no);
                        }
                    }
        });
    }
    private void addItemsCorrectly(){
        progress_bar.setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error in Sending Order !x!");

        final TextView input = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText("We're Sorry, but you need to fill the orders correctly. There seems to be an error or incomplete error");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void cantAddMoreOrders(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error !x!");

        final TextView input = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText("We're Sorry, but you cannot add more orders");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void updateOrderCount(){
        number_of_orders++;
    }
    private void addNewOrder(){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                57
        );
        lp.setMarginStart(0);
        lp.setMarginEnd(10);
        lp.setMargins(0, 10, 0, 30);
        layout.setLayoutParams(lp);

        AutoCompleteTextView item_name = new AutoCompleteTextView(this);
        item_name.setBackground(getResources().getDrawable(R.drawable.border));
        LinearLayout.LayoutParams item_name_params = new LinearLayout.LayoutParams(450, LinearLayout.LayoutParams.WRAP_CONTENT);
        item_name_params.setMarginStart(15);
        item_name_params.setMarginEnd(3);
        item_name_params.weight = 3;
        item_name.setTextColor(Color.parseColor("#000000"));
        item_name.setVisibility(View.VISIBLE);
        item_name.setLayoutParams(item_name_params);

        AutoCompleteTextView item_quantity = new AutoCompleteTextView(this);
        item_quantity.setBackground(getResources().getDrawable(R.drawable.border));
        LinearLayout.LayoutParams item_quantity_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        item_quantity_params.setMarginStart(2);
        item_name_params.setMarginEnd(15);
        item_quantity.setTextColor(Color.parseColor("#000000"));
        item_quantity_params.weight = 3;
        item_quantity.setVisibility(View.VISIBLE);
        item_quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        item_quantity.setLayoutParams(item_quantity_params);

        if(number_of_orders == 8){
            item_name.setId(R.id.etItemName8);
            item_quantity.setId(R.id.etItemQuantity8);
        }
        else if (number_of_orders == 9){
            item_name.setId(R.id.etItemName9);
            item_quantity.setId(R.id.etItemQuantity9);
        }
        else if (number_of_orders == 10){
            item_name.setId(R.id.etItemName10);
            item_quantity.setId(R.id.etItemQuantity10);
        }
        else if (number_of_orders == 11){
            item_name.setId(R.id.etItemName11);
            item_quantity.setId(R.id.etItemQuantity11);
        }
        else if (number_of_orders == 12){
            item_name.setId(R.id.etItemName12);
            item_quantity.setId(R.id.etItemQuantity12);
        }
        else if (number_of_orders == 13){
            item_name.setId(R.id.etItemName13);
            item_quantity.setId(R.id.etItemQuantity13);
        }
        else if (number_of_orders == 14){
            item_name.setId(R.id.etItemName14);
            item_quantity.setId(R.id.etItemQuantity14);
        }
        else if (number_of_orders == 15){
            item_name.setId(R.id.etItemName15);
            item_quantity.setId(R.id.etItemQuantity15);
        }

        layout.addView(item_name);
        layout.addView(item_quantity);

        vertical_layout.addView(layout);
        Toast.makeText(CitizenPurchaseActivity.this, "Added a new Order to the List", Toast.LENGTH_SHORT).show();
    }
}
