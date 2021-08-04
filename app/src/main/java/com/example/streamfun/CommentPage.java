package com.example.streamfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class CommentPage extends AppCompatActivity {
    EditText commenttext;
    Button commentsubmit;
    DatabaseReference userref, commentref;
    String postkey;
    RecyclerView recview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);
        getSupportActionBar().hide();
        commentsubmit = findViewById(R.id.cmnt_btn);
        commenttext = findViewById(R.id.cmnt_textView);
        recview = findViewById(R.id.cmnt_rcview);
        postkey = getIntent().getStringExtra("postkey");
        userref = FirebaseDatabase.getInstance().getReference().child("userProfiles");
        commentref = FirebaseDatabase.getInstance().getReference().child("myVideos").child(postkey).child("comment");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = firebaseUser.getUid();

        recview=(RecyclerView)findViewById(R.id.cmnt_rcview);
        recview.setLayoutManager(new LinearLayoutManager(this));


        commentsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userref.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String username=snapshot.child("uname").getValue().toString();
                            String uimage=snapshot.child("uimage").getValue().toString();
                            processcomment(username,uimage);
                        }
                        commenttext.setText("");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void processcomment(String username, String uimage)
            {
                String commentpost=commenttext.getText().toString();
                String randompostkey=userId+""+new Random().nextInt(1000);

                Calendar datevalue=Calendar.getInstance();
                SimpleDateFormat dateFormat=new SimpleDateFormat("dd-mm-yy");
                String cdate=dateFormat.format(datevalue.getTime());

                SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
                String ctime=timeFormat.format(datevalue.getTime());

                HashMap cmnt=new HashMap();
                cmnt.put("uid",userId);
                cmnt.put("username",username);
                cmnt.put("userimage",uimage);
                cmnt.put("usermsg",commentpost);
                cmnt.put("date",cdate);
                cmnt.put("time",ctime);

                commentref.child(randompostkey).updateChildren(cmnt)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful())
                                    Toast.makeText(getApplicationContext(),"Comment Added",Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(),task.toString(),Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<CommentModel> options =
                new FirebaseRecyclerOptions.Builder<CommentModel>()
                        .setQuery(commentref, CommentModel.class)
                        .build();

        FirebaseRecyclerAdapter<CommentModel,CommentViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<CommentModel, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull CommentModel model) {
                holder.cuname.setText(model.getUsername());
                holder.cumessage.setText(model.getUsermsg());
                holder.cudt.setText("Date :"+model.getDate()+" Time :"+model.getTime());
                Glide.with(holder.cuimage.getContext()).load(model.getUserimage()).into(holder.cuimage);
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_single_row,parent,false);
                return  new CommentViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        recview.setAdapter(firebaseRecyclerAdapter);

    }
}
