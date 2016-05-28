package ubru.sabaipon.teerawat.apichat.herbbanban;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    //Explicit
    private MyManage myManage;
    private EditText userEditText, passwordEditText;
    private String userString, passwordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind Widget
        userEditText = (EditText) findViewById(R.id.editText);
        passwordEditText = (EditText) findViewById(R.id.editText2);


        //Request Database
        myManage = new MyManage(MainActivity.this);

        //Test Add Value
        //testAddValue();

        //Delete SQLite
        deleteSQLite();

        //Syn JSON
        synJSON();

    }   // Main Method



    private void synJSON() {

        //Connected Http
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy
                .Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        int intIndex = 0;
        while (intIndex <= 1) {

            //1 Create InputStream
            InputStream inputStream = null;
            String[] urlStrings = {"http://swiftcodingthai.com/herb/php_get_user.php",
                    "http://swiftcodingthai.com/herb/php_get_herb.php"};

            try {

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(urlStrings[intIndex]);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                inputStream = httpEntity.getContent();

            } catch (Exception e) {
                Log.d("herb", "Error1 ==> " + e.toString());
            }

            //2 Create strJSON
            String strJSON = null;

            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String strLine = null;

                while ((strLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(strLine);
                }
                inputStream.close();
                strJSON = stringBuilder.toString();

            } catch (Exception e) {
                Log.d("herb", "Error2 ==> " + e.toString());
            }


            //3 Update SQLite


            try {

                JSONArray jsonArray = new JSONArray(strJSON);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    switch (intIndex) {

                        case 0:

                            String strColumn1 = jsonObject.getString(MyManage.column_User);
                            String strColumn2 = jsonObject.getString(MyManage.column_Password);
                            String strColumn3 = jsonObject.getString(MyManage.column_Status);
                            String strColumn4 = jsonObject.getString(MyManage.column_Name);
                            String strColumn5 = jsonObject.getString(MyManage.column_Surname);
                            String strColumn6 = jsonObject.getString(MyManage.column_Phone);
                            String strColumn7 = jsonObject.getString(MyManage.column_Address);

                            myManage.addValueToSQLite(0, strColumn1, strColumn2, strColumn3,
                                    strColumn4, strColumn5, strColumn6, strColumn7);

                            break;
                        case 1:

                            String strColumn8 = jsonObject.getString(MyManage.column_Name);
                            String strColumn9 = jsonObject.getString(MyManage.column_Image);
                            String strColumn10 = jsonObject.getString(MyManage.column_Description);
                            String strColumn11 = jsonObject.getString(MyManage.column_HowTo);
                            String strColumn12 = jsonObject.getString(MyManage.column_Lat);
                            String strColumn13 = jsonObject.getString(MyManage.column_Lng);
                            String strColumn14 = jsonObject.getString(MyManage.column_Status);

                            myManage.addValueToSQLite(1, strColumn8, strColumn9, strColumn10,
                                    strColumn11, strColumn12, strColumn13, strColumn14);

                            break;

                    }   // switch

                }   // for

            } catch (Exception e) {
                Log.d("herb", "Error3 ==> " + e.toString());
            }

            intIndex += 1;
        } //while

    }   // synJSON

    private void deleteSQLite() {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.database_name,
                MODE_PRIVATE, null);
        sqLiteDatabase.delete(MyManage.user_table, null, null);
        sqLiteDatabase.delete(MyManage.herb_table, null, null);

    }   // delete


    private void testAddValue() {
        int intTime = 0;
        while (intTime <= 1) {

            myManage.addValueToSQLite(intTime, "test1", "test2", "test3",
                    "test4", "test5", "test6", "test7");

            intTime += 1;
        }
    }

    public void clickSignInMain(View view) {

        userString = userEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();

        //Check Space
        if (userString.equals("") || passwordString.equals("")) {
            MyAlertDialog myAlertDialog = new MyAlertDialog();
            myAlertDialog.myDialog(this, "มีช่องว่าง", "กรุณากรอกทุกช่อง คะ");
        } else {
            checkUser();
        }

    }   // signIn

    private void checkUser() {

        try {

            SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.database_name,
                    MODE_PRIVATE, null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM userTABLE WHERE User = " + "'" + userString + "'", null);
            cursor.moveToFirst();
            String[] resultStrings = new String[cursor.getColumnCount()];
            for (int i = 0; i < cursor.getColumnCount(); i++) {

                resultStrings[i] = cursor.getString(i);

            }   // for
            cursor.close();

            //Check Password
            if (passwordString.equals(resultStrings[2])) {
                Toast.makeText(this, "ยินดีต้อนรับ " + resultStrings[4], Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, ReadAllHerbActivity.class);
                intent.putExtra("Data", resultStrings);
                startActivity(intent);
                finish();

            } else {
                MyAlertDialog myAlertDialog = new MyAlertDialog();
                myAlertDialog.myDialog(this, "Password False",
                        "กรุณากรอกใหม่ Password ผิด");
            }


        } catch (Exception e) {
            MyAlertDialog myAlertDialog = new MyAlertDialog();
            myAlertDialog.myDialog(this, "หาไม่พบ",
                    "ไม่มี " + userString + " ในฐานข้อมูลของเรา");
        }

    }   // checkUser

    public void clickSignUpMain(View view) {
        startActivity(new Intent(MainActivity.this,
                SignUpActivity.class));
    }

    public void clickGuest(View view) {

        Intent intent = new Intent(this, HerbListView.class);
        intent.putExtra("Status", "1");
        startActivity(intent);

    }

}   // Main Class
