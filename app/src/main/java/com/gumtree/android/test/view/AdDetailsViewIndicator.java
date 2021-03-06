package com.gumtree.android.test.view;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.gumtree.android.test.R;
import com.gumtree.android.test.controller.listener.AdDetailsUserEventListener;
import com.gumtree.android.test.model.bean.Ad;
import com.gumtree.android.test.model.bean.Contact;
import com.gumtree.android.test.view.adapter.ImageViewPagerAdapter;
import com.gumtree.android.test.view.widget.AdDetailsMetadataView;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Uses android views to display the UI for the Ad Details.
 */
public class AdDetailsViewIndicator implements AdDetailsIndicator, View.OnClickListener {

    private static final float DEFAULT_ZOOM_LEVEL = 12;
    private final Resources mRes;
    
    private AdDetailsUserEventListener mAdDetailsUserEventListener;
    
    private View mMainContent;
    private View mContactLayout;
    private View mEmptyText;
    private View mProgress;
    private TextView mPicsNumber;
    private TextView mTitle;
    private TextView mLocationText;
    private TextView mPriceText;
    private AdDetailsMetadataView mMetadataView;
    private TextView mDescription;
    private TextView mReferenceNumber;
    private TextView mContactPrompt;
    private TextView mContactPostingPeriod;
    private View mCallButton;
    private View mSmsButton;
    private View mEmailButton;
    private ViewPager mImagePager;
    private MapView mMapView;
    private GoogleMap mMap;

    public AdDetailsViewIndicator(Resources res) {
        mRes = res;
    }

    @Override
    public View initialize(LayoutInflater inflater, ViewGroup container,
            AdDetailsUserEventListener adDetailsUserEventListener, Bundle savedInstanceState) {
        mAdDetailsUserEventListener = adDetailsUserEventListener;
        
        View rootView = inflater.inflate(R.layout.fragment_ad_details, container, false);
        mImagePager = (ViewPager)rootView.findViewById(R.id.image_viewpager);
        mMainContent = rootView.findViewById(R.id.main_scrollview);
        mContactLayout = rootView.findViewById(R.id.contact_layout);
        mEmptyText = rootView.findViewById(R.id.empty_text);
        mProgress = rootView.findViewById(R.id.progress);
        mPicsNumber = (TextView)rootView.findViewById(R.id.pics_number);
        mTitle = (TextView)rootView.findViewById(R.id.title);
        mLocationText = (TextView)rootView.findViewById(R.id.location_text);
        mPriceText = (TextView)rootView.findViewById(R.id.price_text);
        mMetadataView = (AdDetailsMetadataView)rootView.findViewById(R.id.ad_details_metadata_view);
        mDescription = (TextView)rootView.findViewById(R.id.description_text);
        mReferenceNumber = (TextView)rootView.findViewById(R.id.reference_number);
        mMapView = (MapView)rootView.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMap = mMapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(false);
        MapsInitializer.initialize(container.getContext());


        mContactPrompt = (TextView)rootView.findViewById(R.id.contact_prompt);
        mContactPostingPeriod = (TextView)rootView.findViewById(R.id.contact_posting_period);
        mCallButton = rootView.findViewById(R.id.call_button);
        mSmsButton = rootView.findViewById(R.id.sms_button);
        mEmailButton = rootView.findViewById(R.id.email_button);


        mCallButton.setOnClickListener(this);
        mSmsButton.setOnClickListener(this);
        mEmailButton.setOnClickListener(this);
        mImagePager.setOnClickListener(this);
        mLocationText.setOnClickListener(this);
        rootView.findViewById(R.id.report_ad_button).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void showProgress() {
        mProgress.setVisibility(View.VISIBLE);
        mMainContent.setVisibility(View.GONE);
        mContactLayout.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.GONE);
    }

    @Override
    public void showAdData(Ad ad) {
        mEmptyText.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
        mMainContent.setVisibility(View.VISIBLE);
        mContactLayout.setVisibility(View.VISIBLE);
        
        int imagesSize = ad.getImageUrls().size();
        if(imagesSize > 0) {
            mImagePager.setAdapter(new ImageViewPagerAdapter(mImagePager.getContext(),
                    ad.getImageUrls(), this));
            mPicsNumber.setText(String.valueOf(imagesSize));
        } else {
            mImagePager.setVisibility(View.GONE);
            mPicsNumber.setVisibility(View.GONE);
        }
        mTitle.setText(ad.getTitle());
        mLocationText.setText(ad.getLocation());
        if(ad.getPrice() > 0) {
            mPriceText.setText(mRes.getString(R.string.price_str, ad.getPrice()));
        } else {
            mPriceText.setVisibility(View.GONE);
        }
        mDescription.setText(ad.getDescription());
        mReferenceNumber.setText(mRes.getString(R.string.reference_number_str, ad.getReference()));
        Contact contact = ad.getContact();
        mContactPrompt.setText(mRes.getString(R.string.contact_prompt_str,
                contact.getName()));
        mContactPostingPeriod.setText(mRes.getString(R.string.contact_posting_period_str,
                contact.getPostingPeriod()));
        String email = contact.getEmail();
        if(email != null) {
            mEmailButton.setTag(email);
        } else {
            mEmailButton.setVisibility(View.GONE);
        }
        String phone = contact.getPhone();
        if(phone != null) {
            mCallButton.setTag(phone);
            mSmsButton.setTag(phone);
        } else {
            mCallButton.setVisibility(View.GONE);
            mSmsButton.setVisibility(View.GONE);
        }
        mMetadataView.populateWith(ad.getMetadataList());

        // Updates the location and zoom of the MapView
        LatLng latLng = new LatLng(ad.getLatitude(), ad.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
        mMap.moveCamera(cameraUpdate);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        mMap.addMarker(options);
        mMapView.postInvalidateDelayed(1000);
        mLocationText.setTag("http://maps.google.com/maps?q=loc:" + ad.getLatitude() + "," + ad.getLongitude() + " (" +
                ad.getTitle() + ")");
    }

    @Override
    public void showError() {
        mEmptyText.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        mMainContent.setVisibility(View.GONE);
        mContactLayout.setVisibility(View.GONE);
    }

    @Override
    public void resumeMap() {
        mMapView.onResume();
    }

    @Override
    public void pauseMap() {
        mMapView.onPause();
    }

    @Override
    public void destroyMap() {
        mMapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report_ad_button:
                break;
            case R.id.call_button:
                mAdDetailsUserEventListener.onCallRequested((String)v.getTag());
                break;
            case R.id.sms_button:
                mAdDetailsUserEventListener.onSmsRequested((String) v.getTag());
                break;
            case R.id.email_button:
                mAdDetailsUserEventListener.onEmailRequested((String) v.getTag());
                break;            
            case R.id.single_image:
                mAdDetailsUserEventListener.onFullscreenImagesRequested(
                        ((ImageViewPagerAdapter)mImagePager.getAdapter()).getAllItems(),
                        mImagePager.getCurrentItem());
                break;
            case R.id.location_text:
                mAdDetailsUserEventListener.onGoToMapsRequested((String)v.getTag());
                break;
        }
    }
}
