package com.example.streamfun;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class HomePage extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    FloatingActionButton fbtn;
    RecyclerView recyclerView;
    DatabaseReference likereference;
    Boolean testclick = false;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }
        setContentView(R.layout.activity_home_page);

        fbtn = findViewById(R.id.fButton);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        likereference= FirebaseDatabase.getInstance().getReference("likes");
        FirebaseRecyclerOptions<Member> options =
                new FirebaseRecyclerOptions.Builder<Member>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("myVideos"), Member.class)
                        .build();

        FirebaseRecyclerAdapter<Member,myViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Member, myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Member model) {
                holder.prepareExoplayer(getApplication(),model.getMyVideoName(),model.getMyVideoUrl());

                holder.setOnClickListen(new myViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //do nothing for now
                        Toast.makeText(getApplicationContext(),"video touched",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //delete video
                        name = getRef(position).getKey();
                        shoeDeleteDialog(name);

                    }
                });


                FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                final String userid=firebaseUser.getUid();
                final String postkey=getRef(position).getKey();

                holder.getlikebuttonstatus(postkey,userid);

                holder.like_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testclick=true;

                        likereference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(testclick==true)
                                {
                                    if(snapshot.child(postkey).hasChild(userid))
                                    {
                                        likereference.child(postkey).child(userid).removeValue();
                                        testclick=false;
                                    }
                                    else
                                    {
                                        likereference.child(postkey).child(userid).setValue(true);
                                        testclick=false;
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }
                });

                holder.cmnt_ibtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomePage.this,CommentPage.class);
                        intent.putExtra("postkey",postkey);
                        startActivity(intent);
                    }
                });

                holder.cmnt_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomePage.this,CommentPage.class);
                        intent.putExtra("postkey",postkey);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row,parent,false);
                return new myViewHolder(view);
            }

        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private void shoeDeleteDialog(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
        builder.setTitle("detete Video");
        builder.setMessage("are you sure to delete?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("myVideos");
                Query query = reference.orderByKey().equalTo(name);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(getApplicationContext(),"video deleted",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void FUploadVideo(View view) {
        Intent intent = new Intent(getApplicationContext(),AddVideo.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public void LogoutMe(MenuItem item) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), MyUserSignUp.class);
        startActivity(intent);
        finish();
    }

    public void ManageProfile(MenuItem item) {
        startActivity(new Intent(HomePage.this,UpdateInfo.class));
    }
}

