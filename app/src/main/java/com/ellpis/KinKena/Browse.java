package com.ellpis.KinKena;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.AlbumArtAdapter;
import com.ellpis.KinKena.Adapters.AlbumArtHorizAdapter;
import com.ellpis.KinKena.Objects.BrowseItems;
import com.ellpis.KinKena.Objects.MiniPlaylist;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.Repository.BrowseRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Browse extends Fragment implements AlbumArtAdapter.ItemClickListener{
    @BindView(R.id.browse_featured_rv)
    RecyclerView featuredRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getFromFirebase();
    }

    private void getFromFirebase() {
        BrowseRepository.getBrowseLinks(returnedData -> {
            List<BrowseItems> browseItemsList = new ArrayList<>();
            for (DocumentSnapshot document : returnedData.getDocuments()) {
                BrowseItems browseItems = document.toObject(BrowseItems.class);
                browseItemsList.add(browseItems);
            }
            displayBrowseItem(browseItemsList);
        });
    }

    private void displayBrowseItem(List<BrowseItems> browseItemsList) {
        Collections.sort(browseItemsList);
        for (BrowseItems browseItems : browseItemsList) {
            createViews(browseItems);
        }
    }

    public void createViews(BrowseItems browseItems) {
        switch (browseItems.getType()) {
            case 0:
                createType0View(browseItems);
                break;
            case 1:
                createType1View(browseItems);
                break;
            case 2:
                createType2View(browseItems);
                break;
        }
    }



    public void createType0View(BrowseItems browseItems) {
        ViewGroup insertPoint = getView().findViewById(R.id.browse_container);

        View headerView = getLayoutInflater().inflate(R.layout.card_browse_section_header, null);
        ((TextView)headerView.findViewById(R.id.card_browse_section_header_textview)).setText(browseItems.getTitle());

        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(0, 16, 0, 16);
        textView.setText(browseItems.getTitle());
        textView.setTextSize(16);

        insertPoint.addView(headerView);
        RecyclerView rv = new RecyclerView(getContext());
        AlbumArtAdapter albumArtAdapter = new AlbumArtAdapter(browseItems.getPlaylists());
        albumArtAdapter.setClickListener(this);
        rv.setAdapter(albumArtAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        insertPoint.addView(rv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void createType1View(BrowseItems browseItems) {
        ViewGroup insertPoint = getView().findViewById(R.id.browse_container);

        View headerView = getLayoutInflater().inflate(R.layout.card_browse_section_header, null);
        headerView.findViewById(R.id.card_browse_section_header_textview).setVisibility(View.GONE);
        insertPoint.addView(headerView);

        RecyclerView rv = new RecyclerView(getContext());
        AlbumArtHorizAdapter albumArtHorizAdapter = new AlbumArtHorizAdapter(browseItems.getPlaylists());
        albumArtHorizAdapter.setClickListener(this);
        rv.setAdapter(albumArtHorizAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        insertPoint.addView(rv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    private void createType2View(BrowseItems browseItems) {
        ViewGroup insertPoint = getView().findViewById(R.id.browse_container);

        View headerView = getLayoutInflater().inflate(R.layout.card_browse_section_header, null);
        ((TextView)headerView.findViewById(R.id.card_browse_section_header_textview)).setText(browseItems.getTitle());

        insertPoint.addView(headerView);
        RecyclerView rv = new RecyclerView(getContext());
        AlbumArtAdapter albumArtAdapter = new AlbumArtAdapter(browseItems.getPlaylists());
        albumArtAdapter.setClickListener(this);
        rv.setAdapter(albumArtAdapter);
        rv.setLayoutManager(new GridLayoutManager(getContext(),3));
        insertPoint.addView(rv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onItemClick(View view, int position, MiniPlaylist playlist) {
        String link = playlist.getLink();
        int index = link.lastIndexOf('/');
        int startIndex = link.substring(0, index).lastIndexOf("/")+1;
        int endIndex =  link.lastIndexOf(".json") >=0?link.lastIndexOf(".json"):link.length();

        String ownerID =link.substring(startIndex, index)+"/"+link.substring(index + 1,endIndex);
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(ownerID,"", false))
                .addToBackStack(null)
                .commit();
    }
}
