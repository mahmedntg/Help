package com.dalal.help.ui.requests;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.dalal.help.utils.UserType;
import com.dalal.help.utils.UserUtils;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RequestsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    List<Request> requests;
    private DatabaseReference requestRef;
    private FirebaseAuth firebaseAuth;
    private RequestAdapter mAdapter;
    private ProgressDialog progressDialog;
    private Spinner serviceTypeSpinner;
    List<String> serviceTypes = new ArrayList<>();
    private String serviceName;
    private RecyclerView recyclerView;
    private User user;
    DatabaseReference userRef;
    FloatingActionButton fab;
    DatabaseReference serviceTypeRef;
    ArrayAdapter<String> dataAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        requestRef = FirebaseDatabase.getInstance().getReference("requests");
        serviceTypes.add("All");
        serviceTypeRef = FirebaseDatabase.getInstance().getReference("services");
        dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, serviceTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        user = UserUtils.getInstance(getActivity()).getUser();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        fab = view.findViewById(R.id.fab);
        serviceTypeSpinner = (Spinner) view.findViewById(R.id.serviceType);
        serviceTypeSpinner.setAdapter(dataAdapter);
        requests = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new RequestAdapter(requests, this);
        recyclerView.setAdapter(mAdapter);

        if (user.getType().equals(UserType.DONATOR.getValue())) {
            fab.show();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    swapFragment();
                }
            });
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
        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceTypeSpinner.setOnItemSelectedListener(this);
    }

    private void displayRequests() {
        Query query = requestRef;
        if (user.getType().equals(UserType.ADMIN.getValue()) || user.getType().equals(UserType.DONOR.getValue())) {
            if (!serviceName.equalsIgnoreCase("All")) {
                query = requestRef.orderByChild("serviceType").equalTo(serviceName);
            }
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
                    if (user.getType().equals(UserType.ADMIN.getValue()) && request.getStatus().equals(RequestStatus.PENDING.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    } else if (user.getType().equals(UserType.DONOR.getValue()) && request.getStatus().equals(RequestStatus.ACCEPTED.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    } else if (user.getType().equals(UserType.DONATOR.getValue()) && !request.getStatus().equals(RequestStatus.COMPLETED.getValue())) {
                        request.setKey(data.getKey());
                        requests.add(request);
                    }
                }
                LayoutAnimationController layout_animation =
                        AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);

                recyclerView.setLayoutAnimation(layout_animation);
                mAdapter.notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();
                progressDialog.hide();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
                progressDialog.hide();
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
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null) fragmentTransaction.detach(fragment);
        fragmentTransaction.replace(R.id.nav_host_fragment, addRequestFragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        serviceName = parent.getItemAtPosition(position).toString();
        displayRequests();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        serviceName = "All";
        displayRequests();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}