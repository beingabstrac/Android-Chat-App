package com.androidchatapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static android.R.color.white;
import static android.graphics.Color.WHITE;

public class Chat extends AppCompatActivity {
    LinearLayout layout;
    Button btn_send;
    EditText message;
    ScrollView scrollView;
    Firebase reference1, reference2;
    ToggleButton toggleButton;

    AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword("chatapp", AesCbcWithIntegrity.saltString("chatsalt".getBytes()));

    public Chat() throws GeneralSecurityException {
    }


    protected String encrypt(String message) throws UnsupportedEncodingException, GeneralSecurityException {
        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(message, keys);
        //store or send to server
        String ciphertextString = cipherTextIvMac.toString();
        return ciphertextString;
    }

    protected String decrypt(String message) throws UnsupportedEncodingException, GeneralSecurityException {
        //Use the constructor to re-create the CipherTextIvMac class from the string:
        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(message);
        String plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
        return plainText;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle(UserDetails.chatWith);

        layout = (LinearLayout) findViewById(R.id.layout);
        btn_send = (Button) findViewById(R.id.btn_send);
        message = (EditText) findViewById(R.id.message);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        toggleButton = (ToggleButton) findViewById(R.id.toggle);


        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://android-chatapp-f5ade.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://android-chatapp-f5ade.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = message.getText().toString();

                if (!messageText.equals("")) {

                    Map<String, String> map = new HashMap<String, String>();

                    if (toggleButton.isChecked()) {

                        try {
                            messageText = "1" + "=" + encrypt(messageText);

                            //Toast.makeText(Chat.this, "ENCRYPT "+ messageText, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e1) {
                            e1.printStackTrace();
                        } catch (GeneralSecurityException e1) {
                            e1.printStackTrace();
                        }
                    } else {

                        messageText = "0" + "=" + messageText;

                    }


                    map.put("message", messageText);

                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if (userName.equals(UserDetails.username)) {
                    addMessageBox(message, 1);
                } else {
                    addMessageBox(message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void addMessageBox(String message, int type) {
        TextView textView1 = new TextView(Chat.this);
        textView1.setGravity(Gravity.END);
        TextView textView2 = new TextView(Chat.this);
        textView2.setGravity(Gravity.START);
        textView2.setTextColor(WHITE);

        String[] parts = message.split("=", 2);


        String raw_text = parts[1];

        if (Integer.parseInt(parts[0]) == 1) {
            parts[1] = "** S.E.C.R.E.T **";
        }

//        if (type == 1) {
//            parts[1] = "You \n" + parts[1];
//        } else {
//            parts[1] = UserDetails.chatWith + "\n" + parts[1];
//        }
        //Toast.makeText(Chat.this, "nik "+ parts[1], Toast.LENGTH_LONG).show();
        textView1.setText(parts[1]);
        textView2.setText(parts[1]);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView1.setLayoutParams(lp);
        textView2.setLayoutParams(lp);

        final String[] m_Text = {""};


        if (Integer.parseInt(parts[0]) == 1) {


            if (type == 1) {
                textView1.setBackgroundResource(R.drawable.encrypt_rounded);
                LinearLayout left = new LinearLayout(this);
                left.setGravity(Gravity.END);
                left.addView(textView1);
                layout.addView(left);
                textView1.setTextColor(getResources().getColor(white));

            } else {
                textView2.setBackgroundResource(R.drawable.encrypt_rounded);
                LinearLayout right = new LinearLayout(this);
                right.setGravity(Gravity.START);
                right.addView(textView2);
                layout.addView(right);
                textView2.setTextColor(getResources().getColor(white));
            }


            final String[] text = {raw_text};

            textView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setMessage("Enter Password");

                    // Set up the input
                    final EditText input = new EditText(Chat.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text[0] = input.getText().toString();
                            //Toast.makeText(Chat.this, "pass: " + UserDetails.password , Toast.LENGTH_LONG).show();
                            if (m_Text[0].equals(UserDetails.password)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);

                                try {
                                    text[0] = decrypt(text[0]);
                                } catch (UnsupportedEncodingException e1) {
                                    Toast.makeText(Chat.this, "UnsupportedEncodingException error", Toast.LENGTH_LONG).show();
                                    e1.printStackTrace();
                                } catch (GeneralSecurityException e1) {
                                    Toast.makeText(Chat.this, "GeneralSecurityException error", Toast.LENGTH_LONG).show();
                                    e1.printStackTrace();
                                }


                                builder.setMessage(text[0]);

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            } else {
                                Toast.makeText(Chat.this, "incorrect password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }


            });

            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                    builder.setTitle("Enter Password");

                    // Set up the input
                    final EditText input = new EditText(Chat.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text[0] = input.getText().toString();
                            //Toast.makeText(Chat.this, "pass: " + UserDetails.password , Toast.LENGTH_LONG).show();
                            if (m_Text[0].equals(UserDetails.password)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);

                                try {
                                    text[0] = decrypt(text[0]);
                                } catch (UnsupportedEncodingException e1) {
                                    Toast.makeText(Chat.this, "UnsupportedEncodingException error", Toast.LENGTH_LONG).show();
                                    e1.printStackTrace();
                                } catch (GeneralSecurityException e1) {
                                    Toast.makeText(Chat.this, "GeneralSecurityException error", Toast.LENGTH_LONG).show();
                                    e1.printStackTrace();
                                }


                                builder.setMessage(text[0]);

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            } else {
                                Toast.makeText(Chat.this, "incorrect password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }


            });
        } else {

            if (type == 1) {
                textView1.setBackgroundResource(R.drawable.rounded_corner1);
                LinearLayout left = new LinearLayout(this);
                left.setGravity(Gravity.END);
                left.addView(textView1);
                layout.addView(left);

            } else {
                textView2.setBackgroundResource(R.drawable.rounded_corner2);
                LinearLayout right = new LinearLayout(this);
                right.setGravity(Gravity.START);
                right.addView(textView2);
                layout.addView(right);
            }


        }


//        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}