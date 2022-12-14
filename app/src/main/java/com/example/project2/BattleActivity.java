package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.eazegraph.lib.charts.BarChart;


//import com.github.mikephil.charting.charts.BarChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.models.BarModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BattleActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance(); // ?????????????????? ?????????????????? ??????
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); // ?????? ????????? ????????? ????????? ????????? ???????????? ??????
    private String userId;
    private DatabaseReference mDatabaseRef1 = FirebaseDatabase.getInstance().getReference();      // ?????????????????? DB??? ???????????? ?????? ????????????
    private LinearLayout llBattle, llresultlayout;
    private int run = 0;

    BarChart chart1;
    BarChart chart2;
    //private ArrayList<BarEntry> dataList1 = new ArrayList<>();
    //private ArrayList<BarEntry> dataList2 = new ArrayList<>();
    //ListView list;
    ArrayList data;
    ArrayAdapter adapter;
    EditText battleId;
    Button battleRequest, chatbutton;

    // ?????? ?????? ??????
    private static ArrayList<ChatItem> chatList;
    private String strUserName = "?????????";
    private Handler mHandler;
    InetAddress serverAddr;
    Socket socket;
    PrintWriter sendWriter;
    private String ip = "172.20.10.5";
    private int port = 7008;
    String UserID;
    EditText message;
    String sendmsg;
    String read;

    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;

    /*@Override
    protected void onStop() {
        if(run == 1) {
            super.onStop();
            try {
                sendWriter.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        recyclerView = findViewById(R.id.chatRecyclerView);
        final UserAccount[] userInfo = {new UserAccount()};
        llBattle = findViewById(R.id.ll_battlelayout);
        llresultlayout = findViewById(R.id.ll_resultlayout);

        /*chatBattleBtn = findViewById(R.id.chatBattleBtn);
        chatBattleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BattleActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });*/

        // ?????? ?????? ???, ?????? ?????? ???.
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                run = 0;
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if (userInfo[0] == null || userInfo[0].equals(null)) {
                    run = 0;
                    llBattle.setVisibility(View.VISIBLE);
                    llresultlayout.setVisibility(View.GONE);
                    Log.d("run ???1 : ", String.valueOf(run));
                } else {
                    run = userInfo[0].getRun();
                    Log.d("run ???2 : ", String.valueOf(run));
                    if (run == 1) {
                        llBattle.setVisibility(View.GONE);
                        llresultlayout.setVisibility(View.VISIBLE);
                        // ?????? ???????????? ?????? ?????? run?????? ?????? ????????????!

                    } else {
                        llBattle.setVisibility(View.VISIBLE);
                        llresultlayout.setVisibility(View.GONE);
                    }
                }
            }
        });

        // ???????????? llBattle
        data = new ArrayList<>();

        // Spinner
        Spinner battleSpinner = (Spinner) findViewById(R.id.battleSpinner);
        ArrayAdapter battleAdapter = ArrayAdapter.createFromResource(this,
                R.array.battleDay, android.R.layout.simple_spinner_item);
        battleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        battleSpinner.setAdapter(battleAdapter);

        String matchDay = battleSpinner.getSelectedItem().toString();
        battleId = findViewById(R.id.battleId);

        // ?????? ?????? ????????????
        long now = System.currentTimeMillis();
        Date date1 = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        String date = sdf.format(date1);

        String str[] = matchDay.split("???");
        String res = "";
        for (int i = 0; i < str.length; i++) {
            res += str[i];
        }
        Log.d("?????? ????????????", res);
        Calendar cal = Calendar.getInstance();     //?????? ????????? ?????? Calendar ??????????????? ?????? getInstance()????????? ??????
        cal.setTime(date1);
        cal.add(Calendar.DATE, Integer.parseInt(res));
        String date5 = sdf.format(cal.getTime());
        // ???????????? ?????? ?????? ???,
        battleRequest = findViewById(R.id.battleRequest);
        String finalRes = res;
        final String[] opUserToken = new String[1];
        battleRequest.setOnClickListener(new View.OnClickListener() {
            private ArrayList<UserAccount> arrayList = new ArrayList<>();

            @Override
            public void onClick(View v) {
                String opponent = battleId.getText().toString();    // ????????? ?????? i

                // ????????? ?????? null?????? ???????????? ??????????????? ?????????!
                if (!opponent.equals("")) {
                    mDatabaseRef1 = mFirebaseDB.getInstance().getReference().child("project").getRef();
                    mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // ?????????????????? ?????????????????? ???????????? ???????????? ???
                            arrayList.clear();
                            String compare;
                            int index = 0;
                            int in1 = 0;
                            for (DataSnapshot ss : snapshot.getChildren()) {
                                UserAccount userAccount = ss.getValue(UserAccount.class);
                                compare = userAccount.getId();
                                if (compare.contains(opponent)) {
                                    index = 1;
                                    opUserToken[0] = userAccount.getIdToken();  // ????????? ????????? ?????? ????????????
                                    final UserAccount[] userInfo = {new UserAccount()};
                                    //????????? ??????
                                    mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
//                                            Toast toast = Toast.makeText(BattleActivity.this, "???????????? ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT);
//                                            toast.show();
                                        }

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            userInfo[0] = snapshot.getValue(UserAccount.class);
                                            if (userInfo[0] == null || userInfo[0].getId() == null || userInfo[0].getId().length() == 0 || userInfo[0].equals(null)) {
                                                Toast toast = Toast.makeText(BattleActivity.this, "???????????? ????????? ???????????? ???????????????.", Toast.LENGTH_SHORT);
                                                toast.show();
                                                return;
                                            } else {
                                                Toast toast = Toast.makeText(BattleActivity.this, "???????????? ????????? ???????????????.", Toast.LENGTH_LONG);
                                                toast.show();
                                                Intent intent = new Intent(BattleActivity.this, BattleActivity.class);
                                                startActivity(intent);
                                                userId = userInfo[0].getId();
                                                BattleInfo battleInfo = new BattleInfo();
                                                battleInfo.setUserId(userId);
                                                Log.d("?????? ????????? ??????", userId);
                                                battleInfo.setMatchDay(date5);
                                                Log.d("????????? ??????", date5);
                                                battleInfo.setOpid(opponent);
                                                Log.d("????????? ??????", opponent);
                                                battleInfo.setStartDay(date);
                                                Log.d("???????????? ??????", date);
                                                battleInfo.setOpToken(opUserToken[0]);
                                                // setValue : DB ????????????(UserAccount)??? ????????? ?????????. (2022-10-21 ??????)
                                                mDatabaseRef.child("battle").child(firebaseUser.getUid()).setValue(battleInfo);
                                                Map<String, Object> taskMap1 = new HashMap<String, Object>();
                                                taskMap1.put("run", 1);
                                                mDatabaseRef.child("project").child(firebaseUser.getUid()).updateChildren(taskMap1);


                                                BattleInfo battleInfo1 = new BattleInfo();
                                                battleInfo1.setUserId(opponent);
                                                //battleInfo1.setBattleId(userId);
                                                battleInfo1.setMatchDay(date5);
                                                battleInfo1.setStartDay(date);
                                                battleInfo1.setOpid(userId);
                                                battleInfo1.setOpToken(firebaseUser.getUid());
                                                // setValue : DB ????????????(UserAccount)??? ????????? ?????????. (2022-10-21 ??????)
                                                mDatabaseRef.child("battle").child(opUserToken[0]).setValue(battleInfo1);

                                                Map<String, Object> taskMap2 = new HashMap<String, Object>();
                                                taskMap2.put("run", 1);
                                                mDatabaseRef.child("project").child(opUserToken[0]).updateChildren(taskMap2);
                                                Toast toast1 = Toast.makeText(BattleActivity.this, "????????? ?????????????????????.", Toast.LENGTH_LONG);
                                                toast1.show();
                                                llBattle.setVisibility(View.GONE);
                                                llresultlayout.setVisibility(View.VISIBLE);
                                                return;
                                            }
                                        }
                                    });

                                } else {
                                    in1 += 1;
                                    if (in1 == 1) {
                                        Toast toast = Toast.makeText(BattleActivity.this, "???????????? ?????? ????????????.", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                                if (index == 0) {
                                    //Toast toast = Toast.makeText(BattleActivity.this, "???????????? ?????? ???????????????.", Toast.LENGTH_SHORT);
                                    //toast.show();
                                }
                            }
                        }

                        // DB ????????????
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast toast = Toast.makeText(BattleActivity.this, "????????? ID??? ?????? ?????? ????????????.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(BattleActivity.this, "????????? ID??? ??????????????????.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        TextView tv_resultStart = findViewById(R.id.tv_resultStart);
        TextView tv_resultEnd = findViewById(R.id.tv_resultEnd);
        TextView tv_myId = findViewById(R.id.tv_myId);
        TextView tv_resultId = findViewById(R.id.tv_resultId);
        final String[] optoken = new String[1];
        final BattleInfo[] battleInfos = {new BattleInfo()};
        chart1 = findViewById(R.id.tab1_chart_1);
        chart2 = findViewById(R.id.tab1_chart_2);

        chart1.clearChart();
        chart2.clearChart();

        //????????? ??????
        mDatabaseRef.child("battle").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                tv_resultStart.setText("????????????");
                tv_resultEnd.setText("????????????");
                tv_myId.setText("????????????");
                tv_resultId.setText("????????????");
                Log.d("??????1", "??????1");
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                battleInfos[0] = snapshot.getValue(BattleInfo.class);
                if (battleInfos[0] == null || battleInfos[0].equals(null)) {
                    tv_resultStart.setText("????????????");
                    tv_resultEnd.setText("????????????");
                    tv_myId.setText("????????????");
                    tv_resultId.setText("????????????");
                    Log.d("??????2", "??????2");
                } else {
                    tv_resultStart.setText(battleInfos[0].getStartDay());
                    tv_resultEnd.setText(battleInfos[0].getMatchDay());
                    tv_myId.setText(battleInfos[0].getUserId());
                    tv_resultId.setText(battleInfos[0].getOpid());
                    optoken[0] = battleInfos[0].getOpToken();

                    // ??????????????? ????????? ?????? ?????? ??????
                    long resultTime;
                    int resultDay;
                    SimpleDateFormat format = new SimpleDateFormat("yyy-mm-dd");
                    Date startDate = null;
                    Log.d("????????? ??? ???????????? ??????", "");
                    try {
                        startDate = format.parse(tv_resultStart.getText().toString());
                        Date endDate = format.parse(tv_resultEnd.getText().toString());
                        resultTime = endDate.getTime() - startDate.getTime();
                        resultDay = (int) (resultTime / (24 * 60 * 60 * 1000));
                        Log.d("????????? ??? ????????????1222222 ??????", String.valueOf(resultDay));
                        int sumpoint = 0;
                        // ?????????

                        for (int i = 1; i <= resultDay; i++) {
                            Log.d("????????? ??? ????????????3333333 ??????", String.valueOf(resultDay));
                            final PointInfo[] pointInfos = {new PointInfo()};
                            final int[] point = {0};
                            SimpleDateFormat fm = new SimpleDateFormat("yyy-mm-dd");

                            Calendar cal = Calendar.getInstance();     //?????? ????????? ?????? Calendar ??????????????? ?????? getInstance()????????? ??????
                            cal.setTime(startDate);
                            cal.add(Calendar.DATE, i);
                            Log.d("????????????", String.valueOf(startDate));
                            String date = fm.format(cal.getTime());
                            //????????? ??????
                            int finalI = i;
                            Log.d("??????1", firebaseUser.getUid());
                            Log.d("??????1", date);
                            mDatabaseRef.child("point").child(firebaseUser.getUid()).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                                    point[0] = 0;
                                    chart1.addBar(new BarModel(date, point[0], 0xFF56B7F1));
                                    //dataList1.add(new BarEntry(finalI, point[0]));
                                }

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                                    if (pointInfos[0] == null || pointInfos[0].equals(null)) {
                                        point[0] = 0;
                                        chart1.addBar(new BarModel(date, point[0], 0xFF56B7F1));
                                        //dataList1.add(new BarEntry(finalI, point[0]));
                                    } else {
                                        point[0] += pointInfos[0].getPoint();
                                        chart1.addBar(new BarModel(date, point[0], 0xFF56B7F1));
                                        //dataList1.add(new BarEntry(finalI, point[0]));
                                        Log.d("??? ?????????" + finalI, String.valueOf(point[0]));
                                        // ?????????!X -> ???????????????
                                        ProgressBar myPoint = findViewById(R.id.myPointBar);
                                        myPoint.setProgress(point[0]);

                                    }
                                }
                            });

                            Log.d("?????????1 - ", String.valueOf(sumpoint));
                        }

                        // ????????????
                        for (int i = 1; i <= resultDay; i++) {
                            final PointInfo[] pointInfos = {new PointInfo()};
                            final int[] point = {0};
                            SimpleDateFormat fm = new SimpleDateFormat("yyyy-mm-dd");

                            Calendar cal = Calendar.getInstance();     //?????? ????????? ?????? Calendar ??????????????? ?????? getInstance()????????? ??????
                            cal.setTime(startDate);
                            cal.add(Calendar.DATE, i);
                            String date = fm.format(cal.getTime());
                            //????????? ??????
                            int finalI = i;
                            Log.d("??????2", optoken[0]);
                            Log.d("??????2", date);
                            mDatabaseRef.child("point").child(optoken[0]).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                                    point[0] = 0;
                                    chart2.addBar(new BarModel(date, point[0], 0xFFFF3366));
                                    //dataList2.add(new BarEntry(finalI, point[0]));
                                }

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pointInfos[0] = snapshot.getValue(PointInfo.class);
                                    if (pointInfos[0] == null || pointInfos[0].equals(null)) {
                                        point[0] = 0;
                                        chart2.addBar(new BarModel(date, point[0], 0xFFFF3366));
                                        //dataList2.add(new BarEntry(finalI, point[0]));
                                    } else {
                                        point[0] += pointInfos[0].getPoint();
                                        Log.d("?????? ?????????" + finalI, String.valueOf(point[0]));
                                        chart2.addBar(new BarModel(date, point[0], 0xFFFF3366));
                                        //dataList2.add(new BarEntry(finalI, point[0]));
                                        // ?????????!X -> ???????????????
                                        ProgressBar opPoint = findViewById(R.id.opPointBar);
                                        opPoint.setProgress(point[0]);
                                    }
                                }
                            });


                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        resultDay = 0;
                    }
                    chart1.startAnimation();
                    chart2.startAnimation();

                }
            }

        });

        long now1 = System.currentTimeMillis();
        Date date4 = new Date(now1);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss");
        String getTime1 = dateFormat2.format(date4);

        ////////////////////////////// ?????? ?????? ////////////////////////////////////////////////////
        chatList = new ArrayList<>();
        mHandler = new Handler();
        LinearLayoutManager manager
                = new LinearLayoutManager(BattleActivity.this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(manager); // LayoutManager ??????
        chatbutton = findViewById(R.id.chatbutton);
        message = findViewById(R.id.message);

        // ?????? ????????? ?????? ????????????
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                strUserName = "????????? ??????";
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if (userInfo[0] == null || userInfo[0].getId() == null || userInfo[0].getId().length() == 0 || userInfo[0].equals(null))
                    strUserName = "????????? ??????";
                else {
                    strUserName = userInfo[0].getName();
                    ChatItem admission = new ChatItem(strUserName + "?????? ?????????????????????.", null, null, ViewType.CENTER_JOIN);
                    chatList.add(admission);
                    chatAdapter = new ChatAdapter(chatList);
                    recyclerView.setAdapter(chatAdapter);  // Adapter ??????
                }
            }
        });

        // ?????? Thread() - ?????? ??????, read/update
        new Thread() {
            public void run() {
                try {
                    serverAddr = InetAddress.getByName(ip);
                    socket = new Socket(serverAddr, port);
                    sendWriter = new PrintWriter(socket.getOutputStream());
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        read = input.readLine();
                        if (read != null) {
                            mHandler.post(new msgUpdate(read));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        chatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmsg = message.getText().toString();

                // ?????? Thread() - ????????? ?????? ????????? ??????
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Log.d("????????????", strUserName);
                            Log.d("????????????", getTime1);
                            Log.d("????????????", sendmsg);
                            sendWriter.println(strUserName + "#@#" + getTime1 + "#@#" + sendmsg + "\n");
                            Log.d("printWriter", "println??????");
                            sendWriter.flush();
                            message.setText("");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    // ????????? ????????? ???(read)??? ????????? ??????
    class msgUpdate implements Runnable{
        private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance(); // ?????????????????? ?????????????????? ??????
        private FirebaseDatabase mFirebaseDB;
        private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); // ?????? ????????? ????????? ????????? ????????? ???????????? ??????
        private String strCompare = "";
        private String str;
        private String id;
        private String time;
        private String msg;
        final UserAccount[] userInfo = {new UserAccount()};

        public msgUpdate(String str) {this.str=str;}

        @Override
        public void run() {
            String[] array = str.split("#@#");

            //??????
            for(int i=0;i<array.length;i++) {
                if(i==0) {
                    id = array[i];
                }
                else if(i==1) {
                    time = array[i];
                }
                else if(i==2) {
                    msg = array[i];
                }
            }
            // ?????? ????????? ?????? ????????????
            mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onCancelled(@NonNull DatabaseError error) { //????????? ????????? ??? ??? ?????? ??? ??????
                    strCompare = "????????? ??????";
                }

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userInfo[0] = snapshot.getValue(UserAccount.class);
                    if(userInfo[0] == null || userInfo[0].getName() == null || userInfo[0].getName().length() == 0 || userInfo[0].equals(null))
                        strCompare = "????????? ??????";
                    else {
                        strCompare = userInfo[0].getName();
                        if(strCompare.equals(id)) {
                            if(msg != null)
                                chatList.add(new ChatItem(msg, null, time, ViewType.RIGHT_CHAT));
                        } else {
                            if(msg != null)
                                chatList.add(new ChatItem(msg, id, time, ViewType.LEFT_CHAT));
                        }
                    }
                    chatAdapter = new ChatAdapter(chatList);
                    //recyclerView.setAdapter(chatAdapter);  // Adapter ?????? (????????????)
                    recyclerView.setAdapter(chatAdapter);  // Adapter ??????
                }
            });
        }
    }
}