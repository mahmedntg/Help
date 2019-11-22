package com.dalal.help.ui.requests;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dalal.help.R;
import com.dalal.help.utils.Request;
import com.dalal.help.utils.RequestAdapter;
import com.dalal.help.utils.RequestStatus;
import com.dalal.help.utils.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RequestsFragment extends Fragment {
    List<Request> requests;
    private DatabaseReference requestRef;
    private FirebaseAuth firebaseAuth;
    private RequestAdapter mAdapter;
    private ProgressDialog progressDialog;
    private boolean adminUser = true;
    private Spinner serviceTypeSpinner;
    List<String> serviceTypes = new ArrayList<>();
    private String serviceName;
    private RecyclerView recyclerView;
    private User user;
    DatabaseReference userRef;
    FloatingActionButton fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        fab = view.findViewById(R.id.fab);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        requestRef = FirebaseDatabase.getInstance().getReference("requests");
        serviceTypeSpinner = (Spinner) view.findViewById(R.id.serviceType);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, serviceTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(dataAdapter);
        serviceTypes.add("All");
        DatabaseReference serviceTypeRef = FirebaseDatabase.getInstance().getReference("services");
        serviceTypeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    serviceTypes.add(postSnapshot.child("name").getValue(String.class));
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        serviceName = "all";
        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serviceName = parent.getItemAtPosition(position).toString();
                getUserRequests();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        requests = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new RequestAdapter(requests, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        userRef = FirebaseDatabase.getInstance().getReference("users");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapFragment();
            }
        });
        return view;
    }

    public void getUserRequests() {
        if (user == null) {
            userRef.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    if (!user.getType().equalsIgnoreCase("donator")) {
                        fab.hide();
                    }
                    displayRequests();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            displayRequests();
        }
    }

    private void displayRequests() {
        Query query = requestRef;
        if (user.getType().equalsIgnoreCase("admin") && !serviceName.equalsIgnoreCase("all")) {
            query = requestRef.orderByChild("serviceType").equalTo(serviceName);

        } else {
            query = !serviceName.equalsIgnoreCase("all") ? requestRef.orderByChild("userServiceType").equalTo(firebaseAuth.getCurrentUser().getUid() + "__" + serviceName)
                    : requestRef.orderByChild("userId").equalTo(firebaseAuth.getCurrentUser().getUid());
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requests.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Request request = data.getValue(Request.class);
                    if (user.getType().equalsIgnoreCase("admin") && request.getStatus().equals(RequestStatus.PENDING.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    } else if (user.getType().equalsIgnoreCase("donor") && request.getStatus().equals(RequestStatus.ACCEPTED.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    } else if (user.getType().equalsIgnoreCase("donator") && !request.getStatus().equals(RequestStatus.COMPLETED.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    }
                }
                LayoutAnimationController layout_animation =
                        AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);

                recyclerView.setLayoutAnimation(layout_animation);
                mAdapter.notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();
                progressDialog.hide();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void deleteItem(int position, String key) {
        requestRef.child(key).removeValue();
        requests.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    public User getUser() {
        return user;
    }

    private void swapFragment() {
        AddRequestFragment addRequestFragment = new AddRequestFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, addRequestFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}