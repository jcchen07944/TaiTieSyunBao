package jcchen.taitiesyunbao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.Frame;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Vector;

import static jcchen.taitiesyunbao.Constant.LANGUAGE_TW;

/**
 * Created by JCChen on 2017/7/13.
 */

public class StoreRecyclerViewAdapter extends RecyclerView.Adapter<StoreRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private Vector<StoreInfo> storeList;
    private RecyclerView store_recyclerView, comment_recyclerView;
    private ViewFlipper pic_info_viewFlipper;
    private FrameLayout store_info_frame;
    private TextView name_textView, tel_textView, address_textView;
    private ImageView comment_imageView;
    private BottomSheetBehavior bottomSheetBehavior;
    private NestedScrollView bottom_sheet;
    private FrameLayout map, filter;
    private RelativeLayout info_relativeLayout, pic_info_relativeLayout;

    public StoreRecyclerViewAdapter(Context context, Vector<StoreInfo> storeList, View view) {
        this.context = context;
        this.storeList = storeList;
        this.store_recyclerView = (RecyclerView) view.findViewById(R.id.store_recyclerView);
        this.pic_info_viewFlipper = (ViewFlipper) view.findViewById(R.id.pic_info_viewFlipper);
        this.store_info_frame = (FrameLayout) view.findViewById(R.id.store_info_frame);
        this.name_textView = (TextView) view.findViewById(R.id.name_textView);
        this.tel_textView = (TextView) view.findViewById(R.id.tel_textView);
        this.address_textView = (TextView) view.findViewById(R.id.address_textView);
        this.comment_imageView = (ImageView) view.findViewById(R.id.comment_imageView);
        this.bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet));
        this.comment_recyclerView = (RecyclerView) view.findViewById(R.id.comment_recyclerView);
        this.bottom_sheet = (NestedScrollView) view.findViewById(R.id.bottom_sheet);
        this.map = (FrameLayout) view.findViewById(R.id.map);
        this.filter = (FrameLayout) view.findViewById(R.id.filter);
        this.info_relativeLayout = (RelativeLayout) view.findViewById(R.id.info_relativeLayout);
        this.pic_info_relativeLayout = (RelativeLayout) view.findViewById(R.id.pic_info_relativeLayout);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.store_cardview, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int index) {
        viewHolder.name_textView.setText(storeList.get(index).getName());
        /*viewHolder.address_textView.setText(context.getResources().getString(R.string.address) +
                " : " + storeList.get(index).getAddress(LANGUAGE_TW));
        viewHolder.tel_textView.setText(context.getResources().getString(R.string.tel) +
                " : " + storeList.get(index).getTel());*/
        ViewTreeObserver viewTreeObserver = viewHolder.pic_viewPager.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams layoutParams = viewHolder.pic_viewPager.getLayoutParams();
                int viewPagerWidth = viewHolder.pic_viewPager.getWidth();
                float viewPagerHeight = (float) (viewPagerWidth * 0.7);

                layoutParams.width = viewPagerWidth;
                layoutParams.height = (int) viewPagerHeight;

                viewHolder.pic_viewPager.setLayoutParams(layoutParams);
                viewHolder.pic_viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        viewHolder.pic_viewPager.setAdapter(
                new StoreImagePagerAdapter(context, storeList.get(index).getImage()));
        for (int i = 0; i < 5; i++) {
            if (i < Double.valueOf(storeList.get(index).getRate()).intValue())
                viewHolder.star_imageView[i].setSelected(true);
            else
                viewHolder.star_imageView[i].setSelected(false);
        }
        if(storeList.get(index).getImage().size() > 0) {
            if (!storeList.get(index).getImage().get(0).getProvider().equals("")) {
                viewHolder.photoby_textView.setVisibility(View.VISIBLE);
                viewHolder.attr_textView.setText(Html.fromHtml("<u>" + storeList.get(index).getImage().get(0).getProvider() + "</u>"));
            } else {
                viewHolder.photoby_textView.setVisibility(View.INVISIBLE);
                viewHolder.attr_textView.setText("");
            }
        }
        setDots(viewHolder, index);

        viewHolder.info_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAnimation(viewHolder, index);
                //setInfoDisplay(viewHolder, index);
                //setCommentView(index);
            }
        });

    }

    @Override
    public int getItemCount() {
        return storeList == null ? 0 : storeList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView store_cardView;
        public TextView name_textView, address_textView, tel_textView, photoby_textView, attr_textView;
        public ViewPager pic_viewPager;
        public ImageView info_imageView, like_imageView;
        public ImageView[] star_imageView = new ImageView[5];
        public LinearLayout sliderDots;

        public ViewHolder(View v) {
            super(v);
            store_cardView = (CardView) v.findViewById(R.id.store_cardView);
            name_textView = (TextView) v.findViewById(R.id.name_textView);
            //address_textView = (TextView) v.findViewById(R.id.address_textView);
            //tel_textView = (TextView) v.findViewById(R.id.tel_textView);
            photoby_textView = (TextView) v.findViewById(R.id.photoby_textView);
            attr_textView = (TextView) v.findViewById(R.id.attr_textView);
            pic_viewPager = (ViewPager) v.findViewById(R.id.pic_viewPager);
            info_imageView = (ImageView) v.findViewById(R.id.info_imageView);
            like_imageView = (ImageView) v.findViewById(R.id.like_imageView);
            star_imageView[0] = (ImageView) v.findViewById(R.id.star0_imageView);
            star_imageView[1] = (ImageView) v.findViewById(R.id.star1_imageView);
            star_imageView[2] = (ImageView) v.findViewById(R.id.star2_imageView);
            star_imageView[3] = (ImageView) v.findViewById(R.id.star3_imageView);
            star_imageView[4] = (ImageView) v.findViewById(R.id.star4_imageView);
            sliderDots = (LinearLayout) v.findViewById(R.id.sliderDots);
        }
    }

    private void setDots(final ViewHolder viewHolder, final int index) {
        final int imageCount = storeList.get(index).getImage().size();
        viewHolder.sliderDots.removeAllViews();
        if (imageCount > 1) {
            viewHolder.pic_viewPager.clearOnPageChangeListeners();
            final ImageView[] dots = new ImageView[imageCount];
            for (int i = 0; i < imageCount; i++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 0, 8, 0);
                dots[i] = new ImageView(context);
                dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nonactive_dot));
                viewHolder.sliderDots.addView(dots[i], params);
            }
            dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));
            viewHolder.pic_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < imageCount; i++) {
                        dots[i].setImageDrawable(ContextCompat.getDrawable(
                                context, R.drawable.nonactive_dot));
                    }
                    dots[position].setImageDrawable(ContextCompat.getDrawable(
                            context, R.drawable.active_dot));
                    if(!storeList.get(index).getImage().get(position).getProvider().equals("")) {
                        viewHolder.attr_textView.setText(Html.fromHtml(
                                "<u>" + storeList.get(index).getImage()
                                        .get(position).getProvider() + "</u>"));
                        viewHolder.photoby_textView.setVisibility(View.VISIBLE);
                    }
                    else {
                        viewHolder.attr_textView.setText("");
                        viewHolder.photoby_textView.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    private void setAnimation(final ViewHolder viewHolder, final int index) {

        StoreImagePagerAdapter adapter = (StoreImagePagerAdapter) viewHolder.pic_viewPager.getAdapter();
        final Vector<Drawable> drawable = adapter.getDrawable();
        final Vector<ImageAttr> image = adapter.getImage();
        for(int i = 0; i < adapter.getCount(); i++) {
            final ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(drawable.get(i) == null) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.loadImage(image.get(i).getImageUrl(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageView.setImageBitmap(loadedImage);
                    }
                });
            }
            else
                imageView.setImageDrawable(drawable.get(i));
            pic_info_viewFlipper.addView(imageView, i);
        }
        pic_info_viewFlipper.setDisplayedChild(viewHolder.pic_viewPager.getCurrentItem());


        //  Initialize position and scale.
        store_info_frame.setVisibility(View.VISIBLE);
        info_relativeLayout.setVisibility(View.INVISIBLE);
        map.setVisibility(View.INVISIBLE);
        pic_info_relativeLayout.setX(viewHolder.store_cardView.getX() +
                viewHolder.pic_viewPager.getX() +
                6 * context.getResources().getDisplayMetrics().density);
        pic_info_relativeLayout.setY(viewHolder.store_cardView.getY() +
                viewHolder.pic_viewPager.getY() +
                9 * context.getResources().getDisplayMetrics().density);
        pic_info_viewFlipper.getLayoutParams().width = viewHolder.pic_viewPager.getWidth();
        pic_info_viewFlipper.getLayoutParams().height = viewHolder.pic_viewPager.getHeight();
        filter.getLayoutParams().width = viewHolder.pic_viewPager.getWidth();
        filter.getLayoutParams().height = viewHolder.pic_viewPager.getHeight();

        final Animation translateAnimation = new TranslateAnimation(0, -pic_info_relativeLayout.getX(),
                                                                    0, -pic_info_relativeLayout.getY());
        final Animation scaleAnimation = new ScaleAnimation(1,
                (float)store_recyclerView.getWidth()/(float)viewHolder.pic_viewPager.getWidth(),
                1, 0.8f);
        final Animation alphaAnimation = new AlphaAnimation(0f, 0.7f);
        translateAnimation.setDuration(1000);
        scaleAnimation.setDuration(1000);
        alphaAnimation.setDuration(1000);

        ViewTreeObserver viewTreeObserver = pic_info_relativeLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.setFillAfter(true);
                animationSet.addAnimation(translateAnimation);
                animationSet.addAnimation(scaleAnimation);
                pic_info_relativeLayout.startAnimation(animationSet);
                filter.animate().alpha(0.7f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        filter.setAlpha(0.7f);
                        pic_info_viewFlipper.setInAnimation(context, R.anim.slide_in_right);
                        pic_info_viewFlipper.setOutAnimation(context, R.anim.slide_out_left);
                        pic_info_viewFlipper.setFlipInterval(2000);
                        pic_info_viewFlipper.startFlipping();
                    }
                }).start();
                pic_info_relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        store_recyclerView.setVisibility(View.GONE);
    }

    private void setInfoDisplay(final ViewHolder viewHolder, final int index) {

        //pic_info_viewPager.setAdapter(viewHolder.pic_viewPager.getAdapter());

        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                ((Activity) context).getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LatLng latLng = new LatLng(Double.valueOf(storeList.get(index).getLatitude()),
                        Double.valueOf(storeList.get(index).getLongitude()));
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng));
                        //.title(storeList.get(index).getName()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                // Disable all gestures on map
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                // Disable marker click
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });
            }
        });

        name_textView.setText(storeList.get(index).getName());
        if(!storeList.get(index).getTel().equals("null"))
            tel_textView.setText(context.getResources().getString(R.string.tel) + ": " +
                                        storeList.get(index).getTel());
        address_textView.setText(context.getResources().getString(R.string.address) + ": " +
                                    storeList.get(index).getAddress(LANGUAGE_TW));


    }

    private void setCommentView(int index) {
        // Set bottom sheet peek height and height.
        store_info_frame.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                comment_imageView.setVisibility(View.VISIBLE);
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) bottom_sheet.getLayoutParams();
                params.height = store_info_frame.getHeight();
                bottom_sheet.setLayoutParams(params);
                bottomSheetBehavior.setPeekHeight((int)(store_info_frame.getHeight() - (200 * context.getResources().getDisplayMetrics().density)));
                store_info_frame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Let comment_recyclerView slide fast.
        comment_recyclerView.setNestedScrollingEnabled(false);

        // Hide bottom sheet initially.
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            boolean init = false;
            float address_y, tel_y, name_y;
            float last_offset = 0;
            float min_distance;
            int State;
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (!init) {
                            init = true;
                            address_y = address_textView.getY();
                            tel_y = tel_textView.getY();
                            name_y = name_textView.getY();
                        }
                        min_distance = bottomSheet.getY() - address_y;
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (!init) {
                            init = true;
                            address_y = address_textView.getY();
                            tel_y = tel_textView.getY();
                            name_y = name_textView.getY();
                        }
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        ((MainActivity) context).setTitleString(name_textView.getText().toString());
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        if(State == BottomSheetBehavior.STATE_EXPANDED)
                            ((MainActivity) context).setTitleImage(ContextCompat.getDrawable(
                                            context, ((MainActivity) context).Toolbar_Image[1]));
                        break;
                }
                State = newState;

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int pic_height = (int)(200 * context.getResources().getDisplayMetrics().density);
                if(bottomSheet.getY() < pic_height) {
                    float alpha;
                    if(last_offset - slideOffset < 0) {  // Slide up
                        // address_textView animation.
                        if (bottomSheet.getY() - address_textView.getY() <= min_distance &&
                                address_textView.getAlpha() > 0) {
                            alpha = 1 - (3 * slideOffset);
                            address_textView.setAlpha(alpha);
                            address_textView.setY(bottomSheet.getY() - min_distance);
                        }

                        // tel_textView animation.
                        if (bottomSheet.getY() - tel_textView.getY() <= min_distance) {
                            alpha = 1 - (1.1f * slideOffset);
                            tel_textView.setAlpha(alpha);
                            tel_textView.setY(bottomSheet.getY() - min_distance);
                        }

                        //  name_textView animation.
                        if(bottomSheet.getY() - name_textView.getY() <= 2 * min_distance) {
                            name_textView.setY(bottomSheet.getY() - 2 * min_distance);
                        }
                    }
                    else {
                        //  resume address_textView
                        if(State == BottomSheetBehavior.STATE_SETTLING) {
                            address_textView.setAlpha(1);
                            address_textView.setY(address_y);
                        }
                        else {
                            alpha = 1 - (3 * slideOffset);
                            address_textView.setAlpha(alpha);
                            address_textView.setY(bottomSheet.getY() - min_distance);
                        }

                        //  resume tel_textView
                        if(bottomSheet.getY() < (tel_y + min_distance)) {
                            alpha = 1 - (1.1f * slideOffset);
                            tel_textView.setAlpha(alpha);
                            tel_textView.setY(bottomSheet.getY() - min_distance);
                        }
                        else {
                            tel_textView.setAlpha(1);
                            tel_textView.setY(tel_y);
                        }

                        //  resume name_textView
                        if(bottomSheet.getY() < (name_y + 2 * min_distance))
                            name_textView.setY(bottomSheet.getY() - 2 * min_distance);
                        else
                            name_textView.setY(name_y);
                    }
                }
                else {
                    if(init) {
                        address_textView.setAlpha(1);
                        address_textView.setY(address_y);
                    }
                }
                last_offset = slideOffset;
            }

        });

        // Control bottom sheet by sliding comment_imageView.
        comment_imageView.setOnTouchListener(new View.OnTouchListener() {
            int last_y = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last_y = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int y = (int) motionEvent.getRawY() - last_y;
                        if(y < 0) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        else if(y > 0) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                        last_y = (int) motionEvent.getRawY();
                        break;
                    }
                    return false;
            }
        });

        Vector<StoreComment> commentList = new Vector<>();
        commentList.add(null);
        RecyclerView.Adapter adapter = new StoreCommentRecyclerViewAdapter(context, commentList);
        new GetStoreComment(adapter, commentList, storeList.get(index)).execute("0");
        comment_recyclerView.setAdapter(adapter);
        comment_recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

}