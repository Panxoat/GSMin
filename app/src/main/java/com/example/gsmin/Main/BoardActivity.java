package com.example.gsmin.Main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gsmin.Adapter.HomeRecyclerViewAdapter;
import com.example.gsmin.Json.JSONTask;
import com.example.gsmin.Model.DB;
import com.example.gsmin.Model.Data;
import com.example.gsmin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BoardActivity extends AppCompatActivity {
    private static HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter();
    private static ArrayList<String[]> listData = new ArrayList<>();

    JSONTask jt;
    private ImageView gsmin;
    private TextView mainText;
    private ImageButton back, floating, search;
    private static EditText mainEdit;
    private static RecyclerView recyclerView;
    private boolean searchActivity= true;
    SweetAlertDialog pDialog;

    public static String channel = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Toolbar toolbar = findViewById(R.id.toolbar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setSupportActionBar(toolbar);

        gsmin = findViewById(R.id.gsmin);
        mainText = findViewById(R.id.mainText);
        floating = findViewById(R.id.fab);
        back = findViewById(R.id.drawer_btn);
        search = findViewById(R.id.searchBtn);
        mainEdit = findViewById(R.id.mainEdit);
        recyclerView = findViewById(R.id.recycler_main_board);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new HomeRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        back.setBackgroundResource(R.drawable.arrow_back);
        gsmin.setVisibility(View.GONE);

        mainText.setVisibility(View.VISIBLE);
        mainText.setText(channel);
        listData = new ArrayList<>();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

        pDialog.getProgressHelper().setBarColor(Color.parseColor("#41AFE5"));
        pDialog.setTitleText("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();

        Intent intent = getIntent();
        if (getIntent()!=null && intent.getExtras()!= null) {
            channel = intent.getExtras().getString("channel");
        }
        getChannel();

        final SwipeRefreshLayout slayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        slayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                listData.clear();
                getChannel();
                mainEdit.setText("");
                search();
                mainText.setVisibility(View.VISIBLE);
                mainEdit.setVisibility(View.GONE);
                slayout.setRefreshing(false);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                intent.putExtra("channel", channel);
                startActivity(intent);
                BoardActivity.this.finish();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
        mainEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getData();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getData();

    }

    private void getChannel() {
        mainText.setText(channel);
        if (channel.equals("채용 공고")){
            Data.type = "GET";
            jt = new JSONTask();
            jt.execute(Data.url + "/gsm_hire_list");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (jt.jsonReturn().equals("null")){
                            findViewById(R.id.no_board_layout).setVisibility(View.VISIBLE);
                            mainText.setText(channel);
                            pDialog.hide();
                            return;
                        }
                        String jsonRt = jt.jsonReturn();
                        JSONArray ja = new JSONArray(jsonRt);
                        findViewById(R.id.no_board_layout).setVisibility(View.GONE);
                        for (int i = 0; i < ja.length(); i++){
                            JSONObject jo = ja.getJSONObject(i);
                            String[] a = new String[]{jo.getString("1"), jo.getString("2"), jo.getString("3"), "0", "0", "null", "채용 공고"};
                            listData.add(i, a);
                        }
                        getData();

                    } catch (JSONException e) {
                        pDialog.hide();

                        SweetAlertDialog sd = new SweetAlertDialog(BoardActivity.this, SweetAlertDialog.ERROR_TYPE);
                        sd.setTitleText("서버 오류 발생...");
                        sd.setContentText("문제가 생겼어요! 잠시만요..");
                        sd.show();
                        sd.findViewById(R.id.confirm_button).setBackgroundColor(ContextCompat.getColor( BoardActivity.this, R.color.skyblue));

                        e.printStackTrace();
                    }
                    Data.type = "POST";
                    mainText.setText(channel);
                    getData();
                    pDialog.hide();
                }
            }, 1000);
        } else {
            Data.setData(
                    new String[]{
                            "page_num",
                            "type"},
                    new String[]{
                            "1",
                            channel
                    });
            jt = new JSONTask();
            jt.execute(Data.url + "/get_board_information");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (jt.jsonReturn().equals("null")){
                            findViewById(R.id.no_board_layout).setVisibility(View.VISIBLE);
                            pDialog.hide();
                            return;
                        }
                        String jsonRt = jt.jsonReturn();
                        JSONArray ja = new JSONArray(jsonRt);
                        findViewById(R.id.no_board_layout).setVisibility(View.GONE);
                        for (int i = 0; i < ja.length(); i++){
                            JSONObject jo = ja.getJSONObject(i);
                            String[] t = jo.getString("date").split("T");
                            String[] a = new String[]{jo.getString("title"), jo.getString("nickname"), t[0], jo.getString("good_count"), jo.getString("comment_count"), jo.getString("idx"), jo.getString("grade")};
                            listData.add(i, a);
                        }
                        mainText.setText(channel);
                        getData();
                        pDialog.hide();
                    } catch (JSONException e) {
                        pDialog.hide();
                        SweetAlertDialog sd = new SweetAlertDialog(BoardActivity.this, SweetAlertDialog.ERROR_TYPE);
                        sd.setTitleText("서버 오류 발생...");
                        sd.setContentText("문제가 생겼어요! 잠시만요..");
                        sd.show();
                        sd.findViewById(R.id.confirm_button).setBackgroundColor(ContextCompat.getColor( BoardActivity.this, R.color.skyblue));
                        e.printStackTrace();
                    }
                }
            }, 1000);
        }
    }

    private static boolean isTextChange(String gdata) {
        if (!gdata.contains( mainEdit.getText().toString() )) {
            return false;
        }else{
            return true;
        }
    }

    public void search(){
        if(searchActivity){
            getData();
            mainText.setVisibility(View.GONE);
            mainEdit.setVisibility(View.VISIBLE);
            searchActivity=false;
        }else {
            getData();
            mainText.setVisibility(View.VISIBLE);
            mainEdit.setVisibility(View.GONE);
            searchActivity=true;
        }
    };

    public static void getData(){
        adapter = new HomeRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        for (int i = 0; i < listData.size(); i++) {
            DB db = new DB();
            if (mainEdit.getText().toString().length() != 0){
                if (isTextChange(listData.get(i)[0])){
                    db.setBoardData(listData.get(i)[0], listData.get(i)[1], listData.get(i)[2], listData.get(i)[3], listData.get(i)[4], listData.get(i)[5], listData.get(i)[6]);
                    adapter.addItem(db);
                }
            }else{
                db.setBoardData(listData.get(i)[0], listData.get(i)[1], listData.get(i)[2], listData.get(i)[3], listData.get(i)[4], listData.get(i)[5], listData.get(i)[6]);
                adapter.addItem(db);
            }
        }
        adapter.notifyDataSetChanged();
        Log.d("BoardActivity", "getData: Adapter");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        BoardActivity.this.finish();
    }
}
