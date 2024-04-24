package com.example.finalexam;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalexam.databinding.FragmentMyFavoritesBinding;
import com.example.finalexam.databinding.ListItemPhotoFavoriteBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MyFavoritesFragment extends Fragment {
    private FirebaseAuth mAuth;

    public MyFavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentMyFavoritesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favAdapter = new FavAdapter();
        binding.recyclerView.setAdapter(favAdapter);
        getActivity().setTitle("My Favorites");
        getFavorites();
    }

    FavAdapter favAdapter;
    ArrayList<Fav> mFavs = new ArrayList<>();

    class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {
        @NonNull
        @Override
        public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemPhotoFavoriteBinding binding = ListItemPhotoFavoriteBinding.inflate(getLayoutInflater(), parent, false);
            return new FavViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
            Fav fav = mFavs.get(position);
            holder.setupUI(fav);
        }

        @Override
        public int getItemCount() {
            return mFavs.size();
        }

        class FavViewHolder extends RecyclerView.ViewHolder {
            ListItemPhotoFavoriteBinding mBinding;
            Fav mFav;
            public FavViewHolder(ListItemPhotoFavoriteBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Fav fav){
                mFav = fav;
                mBinding.textViewCreatedAt.setText(mFav.getCreatedAt());
                mBinding.textViewDescription.setText(mFav.getDescription());
                mBinding.textViewUserFullName.setText(mFav.getUsername());

                if(fav.getProfimageurl() != "null") {
                    Picasso.get().load(fav.getProfimageurl()).into(mBinding.imageViewUserThumbnail);
                }
                if(fav.getThumburl() != "null") {
                    Picasso.get().load(fav.getThumburl()).into(mBinding.imageViewThumbnail);
                }



                if(mFav.getUid().equals(mAuth.getCurrentUser().getUid())) {
                    mBinding.imageViewTrash.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageViewTrash.setVisibility(View.INVISIBLE);
                }


                mBinding.imageViewTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        //realtime data
                        db.collection("photofavorites").document(fav.getId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Log.d("demo", "onComplete: delete");
                                            //getPosts();
                                        } else {
                                            Toast.makeText(getActivity(), "Error deleting post", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });

                mBinding.getRoot().setOnClickListener(v -> {
                    //mListener.openFav(mFav);
                });
            }
        }

    }

    private void getFavorites() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //realtime data
        db.collection("photofavorites")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        mFavs.clear();
                        for(QueryDocumentSnapshot document: value) {
                            Fav fav = new Fav();
                            fav.setId(document.getId());
                            fav.setDescription(document.getString("description"));
                            fav.setCreatedAt(document.getString("createdAt"));
                            fav.setUsername(document.getString("username"));
                            fav.setProfimageurl(document.getString("profImageUrl"));
                            fav.setThumburl(document.getString("thumbUrl"));
                            fav.setUid(document.getString("uid"));
                            mFavs.add(fav);
                            favAdapter.notifyDataSetChanged();
                        }
                        //favAdapter.notifyDataSetChanged();
                    }
                });
    }

    FavListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (FavListener) context;
    }

    interface FavListener {

        void openFav(Fav fav);
    }

}