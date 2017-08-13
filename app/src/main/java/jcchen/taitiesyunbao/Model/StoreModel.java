package jcchen.taitiesyunbao.Model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jcchen.taitiesyunbao.Presenter.OnStoreListener;
import jcchen.taitiesyunbao.Presenter.StorePresenter;

/**
 * Created by JCChen on 2017/8/10.
 */

public class StoreModel {

    private OnStoreListener onStoreListener;

    public StoreModel(OnStoreListener onStoreListener) {
        this.onStoreListener = onStoreListener;
    }

    public void getStoreInfo (LatLng latLng) {

    }

    public void getStoreInfo (String place_name) {
        final List<String> tackPool = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Stores").child("Stations").child(place_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                    tackPool.add(ds.getKey());
                onStoreListener.onInfoSuccess(tackPool);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}