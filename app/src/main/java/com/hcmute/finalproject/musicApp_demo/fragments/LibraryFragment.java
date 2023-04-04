package com.hcmute.finalproject.musicApp_demo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcmute.finalproject.musicApp_demo.Adapter.CustomAdapter;
import com.hcmute.finalproject.musicApp_demo.MainActivity;
import com.hcmute.finalproject.musicApp_demo.R;
import com.hcmute.finalproject.musicApp_demo.SongActivity;
import com.hcmute.finalproject.musicApp_demo.databinding.FragmentLibraryBinding;
import com.hcmute.finalproject.musicApp_demo.helper.FirebaseAPI;
import com.hcmute.finalproject.musicApp_demo.helper.helper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LibraryFragment extends Fragment implements helper {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentLibraryBinding binding;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LibrabryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLibraryBinding.inflate(inflater, container, false);

        FirebaseAPI firebaseAPI = new FirebaseAPI((SongActivity) getActivity());
        firebaseAPI.retrieveSongs(this);

        return binding.getRoot();
    }

    @Override
    public void onCallback(CustomAdapter customAdapter) {
        Log.d("MusicFragment", "onCallback: " + customAdapter);
        RecyclerView recyclerView = binding.recyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((SongActivity) getActivity());
        if (customAdapter == null) {
            System.out.println("customAdapter is null");
        } else {
            System.out.println("customAdapter is not null");
            binding.emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}