package com.dozuki.ifixit.ui.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.util.Utils;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

public class GalleryActivity extends BaseActivity {

   public static final String MEDIA_FRAGMENT_PHOTOS = "MEDIA_FRAGMENT_PHOTOS";
   public static final String ACTIVITY_RETURN_MODE = "ACTIVITY_RETURN_ID";

   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   public static final String MEDIA_RETURN_KEY = "MEDIA_RETURN_KEY";
   public static final String ATTACHED_MEDIA_IDS = "ATTACHED_MEDIA_IDS";

   public static boolean showingLogout;
   public static boolean showingHelp;
   public static boolean showingDelete;

   private HashMap<String, MediaFragment> mMediaCategoryFragments;
   private MediaFragment mCurrentMediaFragment;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication) getApplication()).getSiteTheme());

      mMediaCategoryFragments = new HashMap<String, MediaFragment>();
      mMediaCategoryFragments.put(MEDIA_FRAGMENT_PHOTOS, new PhotoMediaFragment());

      /*
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_VIDEOS,
       * new VideoMediaFragment());
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_EMBEDS,
       * new EmbedMediaFragment());
       */
      mCurrentMediaFragment = mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);

      showingHelp = false;
      showingLogout = false;
      showingDelete = false;

      boolean getMediaItemForReturn = false;

      if (getIntent().getExtras() != null) {
         Bundle bundle = getIntent().getExtras();
         int returnValue = bundle.getInt(ACTIVITY_RETURN_MODE, -1);
         ArrayList<Integer> alreadyAttachedImages = bundle.getIntegerArrayList(ATTACHED_MEDIA_IDS);
         mCurrentMediaFragment.setAlreadyAttachedImages(alreadyAttachedImages);
         if (returnValue != -1) {
            getMediaItemForReturn = true;
         }
         startActionMode(new ContextualMediaSelect());
      }

      mCurrentMediaFragment.setForReturn(getMediaItemForReturn);

      super.onCreate(savedInstanceState);

      setContentView(R.layout.gallery_root);
      StepAdapter stepAdapter = new StepAdapter(this.getSupportFragmentManager());
      ViewPager pager = (ViewPager) findViewById(R.id.gallery_view_body_pager);
      pager.setAdapter(stepAdapter);
      TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.gallery_view_top_bar);
      titleIndicator.setViewPager(pager);
      pager.setCurrentItem(1);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(SHOWING_DELETE, showingDelete);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      boolean isLoggedIn = ((MainApplication) getApplication()).isUserLoggedIn();
      switch (item.getItemId()) {
         case R.id.top_camera_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchCamera();
            return true;
         case R.id.top_gallery_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchImageChooser();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      if (MainApplication.get().isFirstTimeGalleryUser()) {
         createHelpDialog().show();
         MainApplication.get().setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.gallery_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

   public class StepAdapter extends FragmentStatePagerAdapter {

      public StepAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public int getCount() {
         return mMediaCategoryFragments.size();
      }

      @Override
      public CharSequence getPageTitle(int position) {
         return Utils.capitalize(getString(R.string.images));
         /*
          * switch (position) {
          * case 0:
          * return "Videos";
          * case 1:
          * return "Photos";
          * case 2:
          * return "Embeds";
          * default:
          * return "Photos";
          * }
          */
      }

      @Override
      public Fragment getItem(int position) {
         return mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mCurrentMediaFragment = (MediaFragment) object;
      }
   }

   private AlertDialog createHelpDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.media_help_messege,
       MainApplication.get().getSite().siteName()))
         .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
            }
         });

      return builder.create();
   }

   public final class ContextualMediaSelect implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         // MenuInflater inflater = getSupportMenuInflater();
         // inflater.inflate(R.menu.gallery_menu, menu);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         finish();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

         return true;
      }
   }
}
