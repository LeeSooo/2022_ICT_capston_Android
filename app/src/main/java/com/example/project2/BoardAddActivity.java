package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BoardAddActivity extends AppCompatActivity {
    private TextView tv_date, tv_name, tv_time;
    private EditText et_content, et_title;
    private Button btn_add;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance(); // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mFirebaseDB.getInstance().getReference();
    private DatabaseReference mDatabaseRef1 = FirebaseDatabase.getInstance().getReference();      // 파이어베이스 DB에 저장시킬 상위 주소위치
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); // 방금 로그인 성공한 유저의 정보를 가져오는 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardadd);

        et_title = findViewById(R.id.et_title);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        tv_name = findViewById(R.id.tv_name);
        et_content = findViewById(R.id.et_content);

        readName();

        // 현재 날짜 가져오기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        String date2 = sdf.format(date);
        tv_date.setText(date2);

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String time = sdfTime.format(date);
        tv_time.setText(time);

        // 회원가입 액티비티 처럼 데이터 삽입만 하면 끝남.
        // 수정은 못 할 것 같은데 시발...

        btn_add = findViewById(R.id.btn_update);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 지금 술 취한 상태로 코드 짜는 내용.(2022-11-04 / 오후 10:30)
                BoardInfo boardInfo = new BoardInfo();
                String idToken = firebaseUser.getUid();
                String title = et_title.getText().toString();
                String date = date2;
                String name = tv_name.getText().toString();
                String content = et_content.getText().toString();
                String field = getIntent().getStringExtra("field");

                boardInfo.setIdToken(idToken);
                boardInfo.setTitle(title);
                boardInfo.setDate(date);
                boardInfo.setName(name);
                boardInfo.setTime(time);
                boardInfo.setContent(content);
                boardInfo.setField(field);

                mDatabaseRef1.child("board").child(field).child(date2+time).setValue(boardInfo);
                Toast toast = Toast.makeText(BoardAddActivity.this, "게시물 저장이 완료되었습니다.", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // 찐 이름 가져오는 메소드
    private void readName() {
        final UserAccount[] userInfo = {new UserAccount()};
        //데이터 읽기
        mDatabaseRef.child("project").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                tv_name.setText("회원");
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if (userInfo[0] == null || userInfo[0].getName() == null || userInfo[0].getName().length() == 0 || userInfo[0].equals(null))
                    tv_name.setText("회원");
                else {
                    tv_name.setText(userInfo[0].getName());
                }
            }
        });
    }

}