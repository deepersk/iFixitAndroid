package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.holoeverywhere.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.util.APIEvent;
import com.ifixit.android.imagemanager.ImageManager;
import com.squareup.otto.Subscribe;

public class GuideCreateStepPortalFragment extends Fragment {
   private static final int NO_ID = -1;
   private static final String CURRENT_OPEN_ITEM = null;
   public static int StepID = 0;
   private ListView mStepList;
   private ImageManager mImageManager;
   private StepAdapter mStepAdapter;
   private TextView mAddStepBar;
   private TextView mEditIntroBar;
   private TextView mReorderStepsBar;
   private GuideCreateObject mGuide;
   private TextView mNoStepsText;
   private GuideCreateStepPortalFragment mSelf;
   private int mCurOpenGuideObjectID;

   public GuideCreateStepPortalFragment(GuideCreateObject guide) {
      super();
      mGuide = guide;
      mSelf = this;
   }

   public GuideCreateStepPortalFragment() {
      super();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication) getActivity().getApplication()).getImageManager();
      }
      mStepAdapter = new StepAdapter();

      mCurOpenGuideObjectID = NO_ID;
      if (savedInstanceState != null) {
         mCurOpenGuideObjectID = savedInstanceState.getInt(CURRENT_OPEN_ITEM);
      }

      // APIService.call((Activity) getActivity(),
      // APIService.getGuideAPICall(mGuide.getGuideid()));
   }

   @Subscribe
   public void onGuideCreated(APIEvent.Guide event) {
      if (!event.hasError()) {
         for (GuideStep gs : event.getResult().getStepList())
            mGuide.getSteps().add(new GuideCreateStepObject(gs));
         mStepAdapter.notifyDataSetChanged();
      } else {

      }
   }

   @Override
   public void onResume() {
      super.onResume();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_steps_portal, container, false);
      mStepList = (ListView) view.findViewById(R.id.steps_portal_list);
      mAddStepBar = (TextView) view.findViewById(R.id.add_step_bar);
      mAddStepBar.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mNoStepsText.getVisibility() == View.VISIBLE)
               mNoStepsText.setVisibility(View.GONE);
            GuideCreateStepObject item = new GuideCreateStepObject(StepID++);
            item.setTitle("Test Step " + StepID);
            // mGuide.getSteps().add(item);
            // mDragListView.invalidateViews();
            launchStepEdit(item);
         }
      });
      mEditIntroBar = (TextView) view.findViewById(R.id.edit_intro_bar);
      mEditIntroBar.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            launchGuideEditIntro();
         }
      });
      mReorderStepsBar = (TextView) view.findViewById(R.id.reorder_steps_bar);
      verifyReorder();

      mNoStepsText = (TextView) view.findViewById(R.id.no_steps_text);

      if (mGuide.getSteps().isEmpty())
         mNoStepsText.setVisibility(View.VISIBLE);
      mStepList.setAdapter(mStepAdapter);
      return view;
   }

   public void invalidateViews() {
      mStepList.invalidateViews();
   }

   private class StepAdapter extends BaseAdapter {

      @Override
      public int getCount() {
         return mGuide.getSteps().size();
      }

      @Override
      public Object getItem(int position) {
         return mGuide.getSteps().get(position);
      }

      @Override
      public long getItemId(int position) {
         return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         GuideCreateStepListItem itemView = (GuideCreateStepListItem) convertView;
         GuideCreateStepObject step = (GuideCreateStepObject) getItem(position);
         itemView = new GuideCreateStepListItem(getActivity(), mImageManager, mSelf, step, position);
         itemView.setTag(step.getStepNum());
         return itemView;
      }

   }

   private void launchStepEdit(ArrayList<GuideCreateStepObject> stepList, int curStep) {
      // GuideCreateStepsEditActivity
      Intent intent = new Intent(getActivity(), GuideCreateStepsEditActivity.class);
      intent.putExtra(GuideCreateStepsEditActivity.GuideKey, mGuide);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_LIST_KEY, stepList);
      intent.putExtra(GuideCreateStepsEditActivity.GUIDE_STEP_KEY, curStep);
      startActivityForResult(intent, GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST);
   }

   private void launchStepEdit(GuideCreateStepObject curStep) {
      ArrayList<GuideCreateStepObject> stepList = new ArrayList<GuideCreateStepObject>();
      stepList.addAll(mGuide.getSteps());
      stepList.add(curStep);
      launchStepEdit(stepList, stepList.size() - 1);
   }

   void launchStepEdit(int curStep) {
      ArrayList<GuideCreateStepObject> stepList = new ArrayList<GuideCreateStepObject>();
      stepList.addAll(mGuide.getSteps());
      launchStepEdit(stepList, curStep);
   }

   private void launchGuideEditIntro() {
      GuideIntroFragment newFragment = new GuideIntroFragment();
      newFragment.setGuideOBject(mGuide);
      FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
      transaction.replace(R.id.guide_create_fragment_steps_container, newFragment);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
   }

   void launchStepReorder() {
      GuideCreateStepReorderFragment newFragment = new GuideCreateStepReorderFragment();
      newFragment.setGuide(mGuide);
      FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
      transaction.replace(R.id.guide_create_fragment_steps_container, newFragment);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST) {
         if (resultCode == Activity.RESULT_OK) {
            GuideCreateObject guide =
               (GuideCreateObject) data.getSerializableExtra(GuideCreateStepsEditActivity.GuideKey);
            if (guide != null) {
               Log.i("StepPortalFragmetn", "non null guide update");
               mGuide = guide;
               mStepAdapter = new StepAdapter();
               verifyReorder();
               mStepList.setAdapter(mStepAdapter);
               mStepList.invalidateViews();
            }
         }
      }
   }

   void deleteStep(GuideCreateStepObject step) {
      mGuide.deleteStep(step);
   }

   void verifyReorder() {
      if (mReorderStepsBar == null || mGuide == null)
         return;

      if (mGuide.getSteps().size() < 2) {
         mReorderStepsBar.setAlpha(0.7f);
         mReorderStepsBar.setOnClickListener(null);
      } else {
         mReorderStepsBar.setAlpha(1.0f);
         mReorderStepsBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               launchStepReorder();
            }
         });
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt(CURRENT_OPEN_ITEM, mCurOpenGuideObjectID);
      super.onSaveInstanceState(savedInstanceState);
   }

   public void onItemSelected(int id, boolean sel) {
      if (!sel) {
         mCurOpenGuideObjectID = NO_ID;
         return;
      }

      if (mCurOpenGuideObjectID != NO_ID) {

         GuideCreateStepListItem view = ((GuideCreateStepListItem) mStepList.findViewWithTag(mCurOpenGuideObjectID));
         if (view != null) {
            view.setChecked(false);
         }

         for (int i = 0; i < mGuide.getSteps().size(); i++) {

            if (mGuide.getSteps().get(i).getStepNum() == mCurOpenGuideObjectID) {
               mGuide.getSteps().get(i).setEditMode(false);
            }
         }
      }
      mCurOpenGuideObjectID = id;
   }
}