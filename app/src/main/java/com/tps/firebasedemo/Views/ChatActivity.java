package com.tps.firebasedemo.Views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tps.firebasedemo.DataModel.FriendlyMessage;
import com.tps.firebasedemo.R;
import com.tps.firebasedemo.ViewHolders.ChattingMessages;

/**
 * Created by Abdullah on 6/3/2018.
 */

public class ChatActivity extends AppCompatActivity {
    LinearLayoutManager mLinearLayoutManager;
    RecyclerView usersRecycler;
    String roomId;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, ChattingMessages> mFirebaseUsersAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        usersRecycler = findViewById(R.id.messages);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        usersRecycler.setLayoutManager(mLinearLayoutManager);
        getIntentData(getIntent().getExtras());
        ImageView mSendButton = (ImageView) findViewById(R.id.send_btn);
        final EditText messageET = (EditText) findViewById(R.id.messageET);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new
                        FriendlyMessage();
                friendlyMessage.setMessage(messageET.getText().toString());
                mFirebaseDatabaseReference.child("ChatRoomList/" + roomId)
                        .push().setValue(friendlyMessage);
                messageET.setText("");
            }
        });
    }

    private void getIntentData(Bundle extras) {
        //TODO get chat room id from bundle and create adapter listener
        roomId = extras.getString("RoomId");
        setup(roomId);
    }

    public void setup(String childId) {
        // New child entries
        progressDialog.show();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage usersList = dataSnapshot.getValue(FriendlyMessage.class);
               /* if (usersList != null) {
                    usersList.setChatRoomId(dataSnapshot.getKey());
                }*/
                return usersList;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child("ChatRoomList/" + childId);
        /*mFirebaseDatabaseReference.child("UsersList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("cons_id5")) {
                    // run some code
                    mFirebaseDatabaseReference.child("UsersList").child("cons_id5").push()
                            .setValue(new UsersChattingList("room6", "name"));
                    Log.d("adding new child", "child");
                } else
                    Log.d("new child", "child");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();
        mFirebaseUsersAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, ChattingMessages>(options) {
            @Override
            public ChattingMessages onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new ChattingMessages(inflater.inflate(R.layout.item_messages, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final ChattingMessages viewHolder,
                                            int position,
                                            final FriendlyMessage friendlyMessage) {
                if (friendlyMessage.getMessage() != null) {
                    viewHolder.message.setText(friendlyMessage.getMessage());
                }

                progressDialog.dismiss();

            }
        };

        mFirebaseUsersAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseUsersAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    usersRecycler.scrollToPosition(positionStart);
                }
            }
        });

        usersRecycler.setAdapter(mFirebaseUsersAdapter);
        mFirebaseUsersAdapter.startListening();
    }

    @Override
    public void onPause() {
        mFirebaseUsersAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseUsersAdapter.startListening();
    }


}
