package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BoardActivity extends AppCompatActivity {
    private long backBtnTime = 0;
    private ArrayAdapter<String> adapter;
    private BottomNavigationView bottomNavi, boardNavi;
    private Button contentBtn;
    private FloatingActionButton btn_write;
    private ImageButton searchBtn;
    private TextView contentText, searchText, boardTitle, tv_title, tv_name, tv_date;
    private String id, name, content, field = "free", search, title, date;
    private final BoardInfo[] boardInfo = {new BoardInfo()};
    private BoardInfo bi;
    final static String[] name1 = {"회원"};
    private static String na = "";

    private ArrayList<BoardInfo> arrayList;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance(); // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mFirebaseDB.getInstance().getReference();
    private DatabaseReference mDatabaseRef1;
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); // 방금 로그인 성공한 유저의 정보를 가져오는 객체
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter1;
    private RecyclerView.LayoutManager layoutManager;

    int version = 1;
    //DatabaseOpenHelper helperBoard, helperUser;
    //SQLiteDatabase databaseBoard, databaseUser;

    //String sql;
    //Cursor cursor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        na = readName();
        tv_title = findViewById(R.id.tv_title);
        tv_name = findViewById(R.id.tv_name);
        tv_date = findViewById(R.id.tv_date);

        //DB
        /*helperBoard = new DatabaseOpenHelper(BoardActivity.this, DatabaseOpenHelper.tableNameBoard, null, version);
        helperUser = new DatabaseOpenHelper(BoardActivity.this, DatabaseOpenHelper.tableName, null, version);
        databaseBoard = helperBoard.getWritableDatabase();
        databaseUser = helperUser.getWritableDatabase();

        sql = "SELECT id FROM "+ helperUser.tableName + " WHERE login = '1'";
        cursor = databaseUser.rawQuery(sql, null);
        cursor.moveToNext();   // 첫번째에서 다음 레코드가 없을때까지 읽음
       id = cursor.getString(0); */

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // 객체에 정보담을 배열
        mDatabaseRef1 = mFirebaseDB.getInstance().getReference().child("board").child(field).getRef();
        mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 파이어베이스 데이터베이스 데이터를 받아오는 곳
                arrayList.clear();
                for(DataSnapshot ss : snapshot.getChildren()){
                    BoardInfo boardInfo1 = ss.getValue(BoardInfo.class);
                    arrayList.add(boardInfo1);
                }
                adapter1.notifyDataSetChanged(); // 리스트에 저장 및 새로고침
            }
            // DB 에러처리
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB에러입니당!~", "bbbbbb");
            }
        });

        adapter1 = new CustomAdapter(arrayList, this);
        recyclerView.setAdapter(adapter1);

        // 리스트 생성
        /*list = findViewById(R.id.list);
        data = new ArrayList<>();
        adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, data);
        list.setAdapter(adapter);
        list.setSelection(adapter.getCount() - 1);

        adapter.notifyDataSetChanged();
        list.setSelection(adapter.getCount() - 1);*/

        boardTitle = findViewById(R.id.boardTitle);

        field = "free";
        //read(field);
        boardTitle.setText("자유 게시판");

        // Field변경 시 저장
        boardNavi = findViewById(R.id.boardNavi);
        boardNavi.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.free:
                        field = "free";
                        read(field);
                        boardTitle.setText("자유 게시판");
                        break;
                    case R.id.diet:
                        field = "diet";
                        read(field);
                        boardTitle.setText("다이어트 게시판");
                        break;
                    case R.id.lean:
                        field = "lean";
                        read(field);
                        boardTitle.setText("린매스업 게시판");
                        break;
                    case R.id.bulk:
                        field = "bulk";
                        read(field);
                        boardTitle.setText("벌크업 게시판");
                        break;
                }
                return true;
            }
        });


        btn_write = findViewById(R.id.btn_write);
        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, BoardAddActivity.class);
                intent.putExtra("field", field);
                startActivity(intent);
            }
        });
        /*contentText = findViewById(R.id.contentText);
        contentText.requestFocus();

        //등록버튼 클릭 시
        contentBtn = findViewById(R.id.contentBtn);
        contentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = contentText.getText().toString();
                if(content.contains("ㅅㅂ") || content.contains("ㅂㅅ") || content.contains("ㅗ") || content.contains("fuck")){
                    // 비속어 필터링
                    Toast toast = Toast.makeText(BoardActivity.this, "비속어는 등록 할 수 없습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                bi = new BoardInfo(readName(), content);
                contentText.setText("");
                // setValue : DB 하위주소(UserAccount)에 정보를 삽입함. (2022-10-21 이수)
                mDatabaseRef.child("board").child(field).push().setValue(bi);
                Toast.makeText(BoardActivity.this, "내용이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                read(field);
            }
        });*/

        searchBtn = findViewById(R.id.searchBtn);
        searchText = findViewById(R.id.searchText);

        //검색버튼 클릭 시
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search = searchText.getText().toString();
                Log.d("정보", search);
                if(!search.equals("")) {
                    arrayList.clear();

                    mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // 파이어베이스 데이터베이스 데이터를 받아오는 곳
                            arrayList.clear();
                            for(DataSnapshot ss : snapshot.getChildren()){
                                BoardInfo boardInfo1 = ss.getValue(BoardInfo.class);
                                title = boardInfo1.getTitle();
                                if(title.contains(search)) {
                                    arrayList.add(boardInfo1);
                                }
                            }
                            adapter1.notifyDataSetChanged(); // 리스트에 저장 및 새로고침
                        }
                        // DB 에러처리
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            arrayList.clear();
                            Toast toast = Toast.makeText(BoardActivity.this, "게시글을 불러올 수가 없습니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        });

        // 바텀 네비게이션
        bottomNavi = findViewById(R.id.bottonNavi);
        bottomNavi.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.action_home:
                        Intent intent = new Intent(BoardActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.action_fitness:
                        Intent intent2 = new Intent(BoardActivity.this, FitnessActivity.class);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.action_board:
                        break;
                    case R.id.action_info:
                        Intent intent3 = new Intent(BoardActivity.this, InfoActivity.class);
                        startActivity(intent3);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                }
                return true;
            }
        });

    }
    // 게시판분류를 찾는 메서드 (게시판 분류)
    private void read(String division) {
        arrayList = new ArrayList<>(); // 객체에 정보담을 배열
        mDatabaseRef1 = mFirebaseDB.getInstance().getReference().child("board").child(field);
        mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 파이어베이스 데이터베이스 데이터를 받아오는 곳
                arrayList.clear();
                for(DataSnapshot ss : snapshot.getChildren()){
                    BoardInfo boardInfo1 = ss.getValue(BoardInfo.class);
                    arrayList.add(boardInfo1);
                }
                adapter1.notifyDataSetChanged(); // 리스트에 저장 및 새로고침
            }

            // DB 에러처리
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB에러입니당!~", "bbbbbb");
            }
        });

        adapter1 = new CustomAdapter(arrayList, this);
        recyclerView.setAdapter(adapter1);

        /*//데이터 읽기
        mDatabaseRef.child("board").child(division).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boardInfo[0] = snapshot.getValue(BoardInfo.class);
                data.clear();

                for(DataSnapshot ss : snapshot.getChildren()){
                    BoardInfo boardInfo1 = ss.getValue(BoardInfo.class);
                    contentId = boardInfo1.getName();
                    content = boardInfo1.getContent();
                    data.add(contentId + " : " + content);
                }

                adapter.notifyDataSetChanged();
                list.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                data.clear();
                boardTitle.setText("게시글을 불러올 수가 없습니다.");
            }
        });*/
    }
    // 찐 이름 가져오는 메소드
    private String readName() {
        final UserAccount[] userInfo = {new UserAccount()};
        //데이터 읽기
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                name1[0] = "회원";
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if (userInfo[0] == null || userInfo[0].getName() == null || userInfo[0].getName().length() == 0 || userInfo[0].equals(null))
                    name1[0] = "회원";
                else {
                    name1[0] = userInfo[0].getName();
                    Log.d("이이이이름", name1[0]);
                }
            }
        });
        Log.d("이이이이름111111111", name1[0]);
        return name1[0];
    }
    @Override
    public void onBackPressed(){
        long curTime = System.currentTimeMillis();
        long gapTime = curTime- backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this,"한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}