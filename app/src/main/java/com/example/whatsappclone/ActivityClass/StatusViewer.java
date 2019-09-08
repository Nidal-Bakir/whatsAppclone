package com.example.whatsappclone.ActivityClass;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapters.VisitStatusAdapter;
import com.example.whatsappclone.AssistanceClass.OnSwipeListener;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;
import com.google.gson.Gson;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class StatusViewer extends Fragment implements View.OnTouchListener {
    private static final String TAG = "StatusViewer";
    private static final String STATUS_ARG = "ARG";
    private Status status;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private CardView listContainer;
    // show the number of visits for my story , contact name
    private TextView viewedCount,contact_name;
    private CircleImageView contactProfile;
    private ImageButton deleteStatus,exit;
    //status image
    private ImageView imageView;
    // for Attach the on onTouch listener and detect the Gesture
    private ConstraintLayout viewer_layout;
    private GestureDetector gestureDetector;
    private DataBase dataBase;
    // for move between status
    private List<Status> statusList;

    public StatusViewer() {
        // Required empty public constructor
    }

    public static StatusViewer newInstance(Status status) {
        StatusViewer fragment = new StatusViewer();
        Bundle args = new Bundle();
        // convert the status object to string using Gson
        args.putString(STATUS_ARG, new Gson().toJson(status));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get the status object from string using Gson
            String myGsonStatus = getArguments().getString(STATUS_ARG);
            status = new Gson().fromJson(myGsonStatus, Status.class);

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status_viewer, container, false);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // link the layout to detect the Gesture on it to (hide/show the viewed list )
        viewer_layout = view.findViewById(R.id.viewer_layout);
        //the list
        recyclerView = view.findViewById(R.id.recyclerViewer);
        imageView = view.findViewById(R.id.viewer_Image);
        deleteStatus=view.findViewById(R.id.deleteStatus);
        exit=view.findViewById(R.id.exit_from_viewer);
        contactProfile=view.findViewById(R.id.viewer_profile_image);
        contact_name=view.findViewById(R.id.viewer_contact_name);
        viewedCount=view.findViewById(R.id.viewedCount);
        listContainer=view.findViewById(R.id.viewer_CardView);
        dataBase=new DataBase(getContext());
        TextView swipeUpTextView=view.findViewById(R.id.swipeUpTextView);
        //status imaeg
        Glide.with(this)
                .load(status.getStatusUrl())
                .into(imageView);
        // profile image
        Glide.with(this)
                .load(dataBase.getUserProfile(null,status.getPhone_number()).getImageUrl())
                .error(R.drawable.ic_default_avatar_profile)
                .into(contactProfile);
        // if the status is my status
        if (status.getPhone_number().equals(UserSettings.PHONENUMBER)) {
            swipeUpTextView.setVisibility(View.VISIBLE);
            contact_name.setText("My story");
            viewer_layout.setOnTouchListener(this);
            gestureDetector = new GestureDetector(getContext(), new OnSwipeListener() {
                @Override
                public boolean onSwipe(Direction direction) {
                    if (direction == Direction.up) {
                        //show list of contact how see the user status
                        mListener.onFragmentIGestureDetector(listContainer, Direction.up);
                    }
                    if (direction == Direction.down) {
                        // hide the list
                        mListener.onFragmentIGestureDetector(listContainer, Direction.down);
                    }
                    return true;
                }
            });
            deleteStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteButtonClickListener();
                }
            });
            // init the list of visits
            List<VisitStatus>visitStatusList=dataBase.getAllVisits();
            VisitStatusAdapter visitStatusAdapter=new VisitStatusAdapter(getContext(),visitStatusList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL, false));
            recyclerView.setAdapter(visitStatusAdapter);
            viewedCount.setText("Viewed by "+visitStatusList.size() +" contacts");
        }else {
            //the status is not mine
            contact_name.setText(dataBase.getContact(null,status.getPhone_number()).getContact_name());
        }
        // close the fragment
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().onBackPressed();

            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onActionHandler(OnFragmentInteractionListener onFragmentInteractionListener) {
        this.mListener = onFragmentInteractionListener;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /*
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     */
    public interface OnFragmentInteractionListener {
        void onFragmentIGestureDetector(CardView viewedList, OnSwipeListener.Direction direction);
        void onDeleteButtonClickListener();
    }
}
