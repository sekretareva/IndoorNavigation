package com.example.mapview;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserData implements Parcelable{
    private String name;
    private int icon = 0;
    private long id = -1;
    private ImageView ivIcon;
    private TextView nameView;

    public UserData(){
        selectId(this.name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
    }

    public UserData(String name){
        this.name = name;
        selectId(this.name);
    }

    public UserData(long id){
        this.id = id;
    }

    public UserData(int id, String name, int icon){
        this.name = name;
        this.id = id;
        this.icon = icon;
    }

    public String getName(){
        return this.name;
    }

    public int getIcon() {
        return this.icon;
    }

    public long getId() {
        return this.id;
    }

    public void setName(long id, TextView tv){
        this.nameView = tv;
        Query getName = FirebaseDatabase.getInstance().getReference("users").orderByKey().equalTo(String.valueOf(id));
        getName.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user: snapshot.getChildren()){
                    writeName((String) user.child("name").getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void setName(String name){
        this.name = name;
    }

    public void updateIcon(int iconNum){
        this.icon = iconNum;
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
        db.child(String.valueOf(id)).child("icon").setValue(iconNum);
    }

    public void setIcon(int iconNum){
        this.icon = iconNum;
    }

    public void setIcon(String name, ImageView iv){
        this.name = name;
        this.ivIcon = iv;
        Query getIcon = FirebaseDatabase.getInstance().getReference("users").orderByChild("name").equalTo(this.name);
        setIcon(getIcon);
    }

    public void setIcon(Long id, ImageView iv){
        this.id  = id;
        this.ivIcon = iv;
        Query getIcon = FirebaseDatabase.getInstance().getReference("users").orderByKey().equalTo(String.valueOf(this.id));
        setIcon(getIcon);
    }

    public void setIcon(Query getIcon){
        final int[] iconRes = {R.drawable.icon1, R.drawable.icon2, R.drawable.icon3};
        getIcon.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot user: dataSnapshot.getChildren()){
                    long iconNum = (long)user.child("icon").getValue();
                    writeIcon(iconRes[(int) (iconNum-1)], (int)iconNum);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void writeIcon(int iconNum, int icon) {
        this.icon = icon;
        this.ivIcon.setImageResource(iconNum);
    }

    public void writeId(String id_string) {
        this.id = Integer.parseInt(id_string);
    }

    public void writeName(String name) {
        this.name = name;
        String nameSeq = (String) this.nameView.getText();
        if (nameSeq.length()>0){
            nameSeq += ", " + name;
            this.nameView.setText(nameSeq);
        }
        else
            this.nameView.setText(name);
    }

    public void selectId(String name){
        Query getId = FirebaseDatabase.getInstance().getReference("users").orderByChild("name").equalTo(name);
        getId.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user: snapshot.getChildren())
                    writeId(user.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected UserData(Parcel in) {
        name = in.readString();
        id = in.readLong();
        icon = (int) in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(id);
        dest.writeLong(icon);

    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
}
