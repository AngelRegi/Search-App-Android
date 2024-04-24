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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalexam.databinding.FragmentSearchBinding;
import com.example.finalexam.databinding.ListItemPhotoSearchBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchFragment extends Fragment {
    private final OkHttpClient client = new OkHttpClient();
    private String searchKey = null;
    private FirebaseAuth mAuth;
    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentSearchBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle("Search");
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        photoAdapter = new PhotoAdapter();
        binding.recyclerView.setAdapter(photoAdapter);
        getFavorites();
        binding.buttonSearch.setOnClickListener(v -> {
            String searchText = binding.editTextSearchKeyword.getText().toString();
            if(searchText.isEmpty()){
                Toast.makeText(getActivity(), "Please enter a search keyword", Toast.LENGTH_SHORT).show();
                return;
            }
            getSearchResults(searchText);

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout_menu_item){
            //logout code goes here ...
            mListener.logout();
            return true;
        } else if(item.getItemId() == R.id.my_favorites_menu_item){
            //my favorites code goes here ...
            mListener.goToFavoritesFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getSearchResults(String query){
        mPhotos.clear();
        photoAdapter.notifyDataSetChanged();
        String url =  "https://api.unsplash.com/search/photos/?client_id=55B2i9_KDXP7TD67kNavNSPtzjm793AIyZEJpj0lGnk&query="+ query+"&per_page=50&orientation=landscape&content_filter=high";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d("demo", "onFailure: failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d("demo", "onResponse: " + body);
                    try {
                        JSONObject json = new JSONObject(body);
                        JSONArray resultJson = json.getJSONArray("results");

                        for(int i=0; i < resultJson.length(); i++) {

                            JSONObject itemObject = resultJson.getJSONObject(i);
                            JSONObject userObject = resultJson.getJSONObject(i).getJSONObject("user");
                            JSONObject urlsObj = resultJson.getJSONObject(i).getJSONObject("urls");
                            JSONObject profileImageObj =userObject.getJSONObject("profile_image");

                            Photo photo = new Photo();
                            photo.setId(itemObject.getString("id"));
                            photo.setDescription(itemObject.getString("description"));
                            photo.setCreatedAt(itemObject.getString("created_at"));
                            photo.setUsername(userObject.getString("name"));
                            photo.setProfimageurl(profileImageObj.getString("small"));
                            photo.setThumburl(urlsObj.getString("thumb"));
                            mPhotos.add(photo);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                photoAdapter.notifyDataSetChanged();
                                // binding.textViewCount.setText(items.length + " Contacts");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    //error handler
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d("demo", "onResponse: " + body);
                }
            }
        });
    }
    PhotoAdapter photoAdapter;
    ArrayList<Photo> mPhotos = new ArrayList<>();

    ArrayList<Fav> favorites = new ArrayList<>();

    ArrayList<String> favlist = new ArrayList<>();

    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemPhotoSearchBinding binding = ListItemPhotoSearchBinding.inflate(getLayoutInflater(), parent, false);
            return new PhotoViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Photo photo = mPhotos.get(position);
            holder.setupUI(photo);
        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ListItemPhotoSearchBinding mBinding;
            Photo mPhoto;
            public PhotoViewHolder(ListItemPhotoSearchBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Photo photo){
                mPhoto = photo;
                mBinding.textViewCreatedAt.setText(photo.getCreatedAt());
                mBinding.textViewDescription.setText(photo.getDescription());
                mBinding.textViewUserFullName.setText(photo.getUsername());
                if(photo.getProfimageurl() != "null") {
                    Picasso.get().load(photo.getProfimageurl()).into(mBinding.imageViewUserThumbnail);
                }
                if(photo.getThumburl() != "null") {
                    Picasso.get().load(photo.getThumburl()).into(mBinding.imageViewThumbnail);
                }

                String url = isLikedByLoggedInUser(photo) ? "@drawable/ic_heart_favorite" :"@drawable/ic_heart_not_favorite";
                int imageResourceDef = getActivity().getResources().getIdentifier(url, null, getActivity().getPackageName());
                mBinding.imageViewFavorite.setImageResource(imageResourceDef);

                AtomicBoolean isClicked = new AtomicBoolean(false);
                mBinding.imageViewFavorite.setOnClickListener(v -> {

                    String uri = !isClicked.get() ? "@drawable/ic_heart_favorite" : "@drawable/ic_heart_not_favorite";
                    int imageResource = getActivity().getResources().getIdentifier(uri, null, getActivity().getPackageName());
                    mBinding.imageViewFavorite.setImageResource(imageResource);
                    if(isClicked.get()) {
                        isClicked.set(false);
                        //liked
                        removeFromFavorites(photo);

                    } else {
                        isClicked.set(true);
                        //unliked
                        addToFavorites(photo);

                    }

                });

                //mBinding.
               /* mBinding.textViewCourseNumber.setText(mPhoto.getCourseNumber());
                mBinding.textViewCourseName.setText(mPhoto.getCourseName());
                mBinding.textViewCourseHours.setText(mPhoto.getCreditHours() + " Credit Hours");
                mBinding.textViewCourseLetterGrade.setText(mPhoto.getCourseGrade());*/
            }
        }

    }
    boolean isLikedByLoggedInUser(Photo photo) {
        boolean rv = false;
        for (Fav fav: favorites) {
            if(fav.uid.equals(mAuth.getCurrentUser().getUid())) {
                rv = true;
                break;
            }
        }
        return rv;
    }
    void addToFavorites(Photo photo) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> favorite = new HashMap<>();
        favorite.put("id", photo.getId());
        favorite.put("description", photo.getDescription());
        favorite.put("createdAt", photo.getCreatedAt());
        favorite.put("username", photo.getUsername());
        favorite.put("profImageUrl", photo.getProfimageurl());
        favorite.put("thumbUrl", photo.getThumburl());
        favorite.put("uid", mAuth.getCurrentUser().getUid());

        db.collection("photofavorites").document(photo.getId()).set(favorite).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }

    void removeFromFavorites(Photo photo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //realtime data
        db.collection("photofavorites").document(photo.getId())
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

    private void getFavorites() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //realtime data
        db.collection("photofavorites")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        favorites.clear();
                        for(QueryDocumentSnapshot document: value) {
                            favlist.add(document.getId());
                            Fav fav = new Fav();
                            fav.setId(document.getId());
                            fav.setDescription(document.getString("description"));
                            fav.setCreatedAt(document.getString("created_at"));
                            fav.setUsername(document.getString("username"));
                            fav.setProfimageurl(document.getString("profImageUrl"));
                            fav.setThumburl(document.getString("thumbUrl"));
                            fav.setUid(document.getString("uid"));
                            favorites.add(fav);
                        }
                        //forumAdapter.notifyDataSetChanged();
                    }
                });
    }

    SearchListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SearchListener) context;
    }

    interface SearchListener{
        void logout();
        void goToFavoritesFragment();
    }
}