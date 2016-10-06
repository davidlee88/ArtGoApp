package com.ArtGo.ArtGoApp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.makeramen.roundedimageview.RoundedImageView;
import com.ArtGo.ArtGoApp.R;
import com.ArtGo.ArtGoApp.listeners.OnTapListener;
import com.ArtGo.ArtGoApp.utils.ImageLoaderFromDrawable;
import com.ArtGo.ArtGoApp.utils.MySingleton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * AdapterLocations is created to display location item.
 * Created using RecyclerView.Adapter.
 */
public class AdapterLocations extends RecyclerView.Adapter<AdapterLocations.ViewHolder> {

    // Create arraylist variables to store data
    private ArrayList<String> mLocationIds;
    private ArrayList<String> mLocationNames;
    private ArrayList<String> mLocationAddresses;
    private ArrayList<String> mLocationDistances;
    private ArrayList<String> mLocationImages;
    private ArrayList<String> mTypeId;
    // Create listener, mContext and imageloader objects,
    // and also variables to store image width and height sizes
    private OnTapListener onTapListener;
    private Context mContext;
    private ImageLoaderFromDrawable mImageLoaderFromDrawable;
    private final ImageLoader IMAGE_LOADER;

    public AdapterLocations(Context context)
    {
        this.mLocationIds = new ArrayList<>();
        this.mLocationNames = new ArrayList<>();
        this.mLocationAddresses = new ArrayList<>();
        this.mLocationDistances = new ArrayList<>();
        this.mLocationImages = new ArrayList<>();
        this.mTypeId = new ArrayList<>();
        mContext = context;

        // Get image width and height sizes
        int mImageWidth = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_width);
        int mImageHeight = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_height);

        mImageLoaderFromDrawable = new ImageLoaderFromDrawable(mContext, mImageWidth, mImageHeight);
        IMAGE_LOADER = MySingleton.getInstance(context).getImageLoader();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        // Set item layout
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_locations, null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTapListener != null)
                    onTapListener.onTapView(position);
            }
        });

        // Set data to view
        viewHolder.mTxtLocationName.setText(mLocationNames.get(position));
        String type = mTypeId.get(position);
        //all locations show the type name
        switch (type){
            case "1": viewHolder.mTxtLocationAddress.setText("Art Work");
                break;
            case "2": viewHolder.mTxtLocationAddress.setText("Fountain");
                break;
            case "3": viewHolder.mTxtLocationAddress.setText("Monument");
                break;
            case "4": viewHolder.mTxtLocationAddress.setText("Art Gallery");
                break;
            case "5": viewHolder.mTxtLocationAddress.setText("Art Museum");
                break;
            case "6": viewHolder.mTxtLocationAddress.setText("Art Event");
                break;
        }

        //viewHolder.mTxtLocationAddress.setText(mLocationAddresses.get(position));

        String mFinalDistance = String.valueOf(mLocationDistances.get(position))+" "+
                mContext.getResources().getString(R.string.km);
        viewHolder.mTxtLocationDistance.setText(mFinalDistance);

        // Set different background color for even and odd item
        if((position % 2) == 0) {
            viewHolder.mLytAdapter.setBackgroundColor(mContext.getResources().
                    getColor(R.color.material_background_color_2));
        }else{
            viewHolder.mLytAdapter.setBackgroundColor(mContext.getResources().
                    getColor(R.color.material_background_color));
        }

        if(mLocationImages.get(position).toLowerCase().contains("http")){
            IMAGE_LOADER.get(mLocationImages.get(position),
                    com.android.volley.toolbox.ImageLoader.
                            getImageListener(viewHolder.mImgLocationImage,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo));

        } else {
            int image = mContext.getResources().getIdentifier(mLocationImages.get(position),
                    "drawable", mContext.getPackageName());

            // Load image lazily
            mImageLoaderFromDrawable.loadBitmap(image, viewHolder.mImgLocationImage);
        }
    }

    @Override
    public int getItemCount()
    {
        return mLocationIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        // Create view objects
        private TextView mTxtLocationName, mTxtLocationAddress, mTxtLocationDistance;
        private RoundedImageView mImgLocationImage;
        private RelativeLayout mLytAdapter;

        public ViewHolder(View v)
        {
            super(v);

            // Connect view objects with view ids in xml
            mTxtLocationName = (TextView) v.findViewById(R.id.txtLocationName);
            mTxtLocationAddress = (TextView) v.findViewById(R.id.txtLocationAddress);
            mTxtLocationDistance = (TextView) v.findViewById(R.id.txtLocationDistance);
            mImgLocationImage = (RoundedImageView) v.findViewById(R.id.imgLocationImage);
            mLytAdapter = (RelativeLayout) v.findViewById(R.id.lytAdapter);
        }
    }

    // Method to update data
    public void updateList(ArrayList<String> locationIds,
                           ArrayList<String> locationNames,
                           ArrayList<String> locationAddresses,
                           ArrayList<String> locationDistances,
                           ArrayList<String> locationImages,
                          ArrayList<String> typeid)
    {

        this.mLocationIds.clear();
        this.mLocationIds.addAll(locationIds);

        this.mLocationNames.clear();
        this.mLocationNames.addAll(locationNames);

        this.mLocationAddresses.clear();
        this.mLocationAddresses.addAll(locationAddresses);

        this.mLocationDistances.clear();
        this.mLocationDistances.addAll(locationDistances);

        this.mLocationImages.clear();
        this.mLocationImages.addAll(locationImages);

        this.mTypeId.clear();
        this.mTypeId.addAll(typeid);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}
