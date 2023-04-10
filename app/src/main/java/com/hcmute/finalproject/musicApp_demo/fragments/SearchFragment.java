package com.hcmute.finalproject.musicApp_demo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.hcmute.finalproject.musicApp_demo.Adapter.CustomAdapter;
import com.hcmute.finalproject.musicApp_demo.databinding.FragmentSearchBinding;
import com.hcmute.finalproject.musicApp_demo.model.Music;
import com.hcmute.finalproject.musicApp_demo.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    FirebaseDatabase database;
    DatabaseReference ref;
    FragmentSearchBinding binding;
    SearchView searchInput;
    CustomAdapter customAdapter;
    List<Music> filteredSongs;
//    FragmentSearchResultBinding bindingResult;

    public SearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
//    public static SearchFragment newInstance(String param1, String param2) {
//        SearchFragment fragment = new SearchFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up db reference
        database = FirebaseDatabase.getInstance();
//        ref = database.getReference("Songs");
        ref = database.getReference().child("Songs");

        // collection ref
        CollectionReference songsRef;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        searchInput = binding.searchView;

        filteredSongs = new ArrayList<>();
//        customAdapter = new CustomAdapter(filteredSongs);
//        customAdapter.context = getContext();
        customAdapter = new CustomAdapter(getContext(), filteredSongs);

        // Set up recycler view
        binding.searchList.setHasFixedSize(true);
        binding.searchList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchList.setAdapter(customAdapter);


//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Song song = dataSnapshot.getValue(Song.class);
//                    Log.d("Song", song.toString());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.e("Search", "this worked: " + newText);
                if (newText != null && !newText.isEmpty()) {
//                    Log.e("Search", "onQueryTextChange: " + newText);
//                    newText = newText.toLowerCase().trim();

                    String searchUpper = newText.toUpperCase();
                    String searchLower = newText.toLowerCase();

                    Query query = ref.orderByChild("lowerCaseTitle").startAt(searchUpper).endAt(searchLower + "\uf8ff");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.e("Search", "onDataChange: " + snapshot);
                            filteredSongs.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Music music = dataSnapshot.getValue(Music.class);
                                filteredSongs.add(music);
                            }
                            customAdapter.updateList(filteredSongs);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Search", "onCancelled: " + error);
                        }
                    });




            };
            return true;
        };
    });

        return binding.getRoot();
    }
}