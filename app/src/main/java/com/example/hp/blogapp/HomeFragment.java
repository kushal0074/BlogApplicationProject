package com.example.hp.blogapp;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_View;
    //Create list of model class type
    private List<BlogPost> blogList;
    private DocumentSnapshot lastVisible;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private BlogRecycleAdapter blogRecycleAdapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private boolean firstPageLoaded = true;
    List<BlogPost> al;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        //Get the recycler View
        blog_list_View = view.findViewById(R.id.blog_list_view);
        blogList = new ArrayList<>();
        blogRecycleAdapter = new BlogRecycleAdapter(blogList);
        al = new ArrayList<>();
        //Set adapter to Recycler View
        blog_list_View.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_View.setAdapter(blogRecycleAdapter);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();
            mauth = FirebaseAuth.getInstance();

            //Get Last item Scrolled in REcyclerView
            blog_list_View.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean lastItem = !recyclerView.canScrollVertically(1);

                    if (lastItem) {
//                Toast.makeText(getContext(),"end of 3 posts",Toast.LENGTH_SHORT).show();
                        loadMorePosts();
                    }
                }
            });


            if (mauth.getCurrentUser() != null) {
                //Order according to date
//                Query firstQuery = firebaseFirestore.collection("Posts")
//                        .orderBy("timeStamp", Query.Direction.DESCENDING)
//                        .limit(3);


                //getActivity bcoz to stop the on scroll listener after page closed bcause it will still call load more post
//                firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//
//                        //get lastVisibile iff first page not loaded at starting
//                        if (firstPageLoaded) {
//                            // Get the last visible documentSnapshot
//                            lastVisible = documentSnapshots.getDocuments()
//                                    .get(documentSnapshots.size() - 1);
//                        }
//
//                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
//
//                            if (doc.getType() == DocumentChange.Type.ADDED) {
//                                //Blog Id ..name same as that is Extender class
//                                String BlogPostId = doc.getDocument().getId();
//                                //USE MODEL CLASS and save one object obtained into Model class list
//                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(BlogPostId);
//
//
//                                if (firstPageLoaded) {
//                                    blogList.add(blogPost);
//                                }
//                                //Add new post to top
//                                else {
//                                    blogList.add(0, blogPost);
//                                }
//
//
//                                blogRecycleAdapter.notifyDataSetChanged();
//                            }
//                        }
//
//                    }
//
//
//                });

                firebaseDatabase = FirebaseDatabase.getInstance();
                databaseReference = firebaseDatabase.getReference("Posts");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                BlogPost blogPost = dataSnapshot.getValue(BlogPost.class);
                                al.add(blogPost);

                            }

                        }
                        Log.e("'Data....","getting");
                        for(int i=0;i<al.size();i++)
                        {
                            Log.e("'Data....",""+al.get(i).getDescription());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
        // Inflate the layout for this fragment
        return view;
    }


    private void loadMorePosts() {

        if (mauth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    //If no more posts than docSnaps will be empty leading to crash
                    if (!documentSnapshots.isEmpty()) {

                        // Get the last visible documentSnapshot
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() - 1);


                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                //USE MODEL CLASS and save one object obtained into Model class list
                                String BlogPostId = doc.getDocument().getId();
                                //USE MODEL CLASS and save one object obtained into Model class list
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(BlogPostId);
                                blogList.add(blogPost);

                                blogRecycleAdapter.notifyDataSetChanged();
                            }
                        }

                    }

                }
            });
        }
    }



}
