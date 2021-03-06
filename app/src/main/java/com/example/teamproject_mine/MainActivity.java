package com.example.teamproject_mine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

//    // 비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    // 구글로그인 result 상수 , 구글로그인 버튼을 클릭하여 startactivityforresult 응답코드로 사용
    private static final int RC_SIGN_IN = 900;

    // 구글api클라이언트
    private GoogleSignInClient googleSignInClient;
    private static final String TAG="BAAM";
    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    // 구글  로그인 버튼
    private SignInButton buttonGoogle;
    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btn_signUp;
    private Button btn_signIn;
    private String email = "";
    private String password = "";
    private String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //파이어베이스 인증객체 선언
        firebaseAuth = FirebaseAuth.getInstance();
        btn_signUp=findViewById(R.id.btn_signUp);
        btn_signIn=findViewById(R.id.btn_signIn);
        buttonGoogle = findViewById(R.id.btn_googleSignIn);
        editTextEmail = findViewById(R.id.et_eamil);
        editTextPassword = findViewById(R.id.et_password);
        //Google 로그인을 앱에 통합
        //GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
// 사용자의 ID, 이메일 주소 및 기본 // 프로필 을 요청하도록 로그인을 구성 합니다. ID 및 기본 프로필은 DEFAULT_SIGN_IN에 포함되어 있습니다.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //구글 api clinet
        // googleSignInOptions에서 지정한 옵션으로 GoogleSignInClient를 빌드합니다
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //google 로그인 버튼
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btn_signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String email=editTextEmail.getText().toString();
                String pwd=editTextPassword.getText().toString();

                if(!email.equals("")&&!pwd.equals("")){
                    loginUser(email,pwd);
                }else{
                    Toast.makeText(MainActivity.this, "이메일과 비밀번호를 입력하세요",Toast.LENGTH_SHORT).show();
                }


            }
        });

        firebaseAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override

            //인증살태가 변경될 때 발생
            //사용자가 로그인한 경우 , 사용자가 로그아웃한 경우 사용자가 변경될때 발생ㅣㅗㅎㅎㅣ
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                Log.d(TAG,user.getUid());
                if(user!=null){
                    Intent intent=new Intent(MainActivity.this, BasicActivity.class);
                    startActivity(intent);
                    finish();
                }else{

                }
            }
        };



        btn_signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });




    }

    private void loginUser(String email, String pwd) {

        firebaseAuth.signInWithEmailAndPassword(email,pwd)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //로그인 성공
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                            FirebaseUser mUser=FirebaseAuth.getInstance().getCurrentUser();
                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if(task.isSuccessful()){
                                                String idToken=task.getResult().getToken();
                                            }else{

                                            }
                                        }
                                    });

                            firebaseAuth.addAuthStateListener(firebaseAuthListener);


                        }else{
                            //로그인 실패
                            Toast.makeText(MainActivity.this, "이메일 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //구글 로그인 버튼응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //구글로그인 성공

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

            }
        }

    }

    //사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID Token을 가져와서
    //수정
    //Firebase 사용자 인증정보로 교환하고 Firebase 사용자 인증정보를 사용해 Firebase에 인증한다.

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //로그인 성공
                            Toast.makeText(MainActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                        } else {
                            //로그인 실패
                            Toast.makeText(MainActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //활동을 초기화할떄 사용자가 현재 로그인 되어있는지 확인
   protected void onStart(){
        super.onStart();
   }


   protected void onStop(){
        super.onStop();
        if(firebaseAuthListener!=null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
   }





}