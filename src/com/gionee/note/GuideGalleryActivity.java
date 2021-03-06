package com.gionee.note;

import com.gionee.note.R;
import com.gionee.note.content.Constants;
import com.gionee.note.content.NoteApplication;
import com.gionee.note.content.StatisticalValue;
import com.gionee.note.database.DBOpenHelper;
import com.gionee.note.utils.CommonUtils;
import com.gionee.note.utils.Log;
import com.gionee.note.utils.Statistics;

import amigo.app.AmigoActionBar;
import amigo.app.AmigoActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

public class GuideGalleryActivity extends AmigoActivity {
	private Gallery mGallery;
	private TextView textView;
	private ImageView backToNote;
	private ImageView confirmView;
	private View titleBack;
	private int bgColor;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		CommonUtils.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guidegallery_white);
		initActionBar();

		mGallery = (Gallery)findViewById(R.id.gallery);
		mGallery.setAdapter(new ImageAdapter(this));

		textView = (TextView) findViewById(R.id.gallery_text);

		bgColor = getIntent().getIntExtra("bgColor", 0);
		mGallery.setSelection(bgColor);

		mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				Intent data = new Intent();
				data.putExtra("bgColor", position);
				setResult(NoteActivity.GUIDEGALLERYACTIVITYRESULTOK, data);
				finish();
			}
		});
		mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				arg1.setAlpha(1);
				bgColor = arg2;
				textView.setText((arg2 + 1) + "/" + ImageAdapter.mImageWhite.length);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private TextView mCustomView;
	//gn pengwei 20121214 add for Common control begin
	private AmigoActionBar actionbar;
	private View mActionBarView;
	private TextView mTitle;
	private ImageButton mOkView;
	private void initActionBar() {
		actionbar = this.getAmigoActionBar();
		// Gionee <lilg><2013-05-24> modify for CR00809680 begin
		// actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.home_title_bg_white));
		// Gionee <lilg><2013-05-24> modify for CR00809680 end
		mActionBarView = LayoutInflater.from(actionbar.getThemedContext())
				.inflate(R.layout.change_bg_action_bar_custom, null);

		actionbar.setIcon(R.drawable.gn_note_actionbar_icon);
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
//		actionbar.setTitle(this.getResources().getString(R.string.note_change_bgcolor));
		actionbar.setCustomView(mActionBarView, new AmigoActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mTitle = (TextView) mActionBarView
				.findViewById(R.id.change_bg_title);
		//Gionee <wangpan><2014-05-21> modify for CR01268739 begin
		String title = this.getResources().getString(R.string.note_change_bgcolor);
		mTitle.setText(CommonUtils.getTitleString(title));
        //Gionee <wangpan><2014-05-21> modify for CR01268739 end
		mOkView = (ImageButton) mActionBarView
				.findViewById(R.id.change_background_ok_view);
		mOkView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				data.putExtra("bgColor", bgColor);
				setResult(NoteActivity.GUIDEGALLERYACTIVITYRESULTOK, data);
				finish();
			}
		});
		mOkView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				CommonUtils.showToast(GuideGalleryActivity.this, getResources()
						.getString(R.string.Ok));
				return true;
			}
		});
	}
	//gn pengwei 20121214 add for Common control end
	
	private MenuItem deleteImageItem;
	private MenuItem createNoteItem;
	private MenuItem shareItem;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.change_background_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(NoteActivity.GUIDEGALLERYACTIVITYRESULTCANCEL);
			finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	/*
	 * class ImageAdapter is used to control gallery source and operation.
	 */
	private static class ImageAdapter extends BaseAdapter{
		private Context mContext;
		private static final Integer[] mImageWhite = {
			R.drawable.change_note_bg_yellow_white,
			R.drawable.change_note_bg_blue_white,
			R.drawable.change_note_bg_green_white,
			R.drawable.change_note_bg_red_white
		};

		public ImageAdapter(Context c){
			mContext = c;
		}
		@Override
		public int getCount() {
			return mImageWhite.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView (mContext);
		    i.setImageResource(mImageWhite[position]);
			i.setScaleType(ImageView.ScaleType.FIT_XY);
		    // Gionee <wangpan><2014-05-15> modify for CR01249465 begin
			i.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			int dip2px = CommonUtils.dip2px(mContext, 15);
			i.setPadding(dip2px, dip2px, dip2px, dip2px);
		    // Gionee <wangpan><2014-05-15> modify for CR01249465 end
			
			return i;
		}

	};
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("GuideGalleryActivity------onResume begin!");
		
		// Gionee <lilg><2013-04-10> add for note upgrade begin
		((NoteApplication) getApplication()).registerVersionCallback(this);
		// Gionee <lilg><2013-04-10> add for note upgrade end
		
		Log.d("GuideGalleryActivity------onResume end!");
		Statistics.onResume(this);
	}
	
	@Override
	protected void onPause() {
		Log.i("GuideGalleryActivity------onPause begin!");
		
		// Gionee <lilg><2013-04-10> add for note upgrade begin
		((NoteApplication) getApplication()).unregisterVersionCallback(this);
		// Gionee <lilg><2013-04-10> add for note upgrade end
		
		Log.d("GuideGalleryActivity------onPause end!");
		super.onPause();
		Statistics.onPause(this);
	}
	
	private void judgeColor(String colorStr) {
		if ("0".equals(colorStr)) {
			Statistics.onEvent(GuideGalleryActivity.this, Statistics.NOTE_BACKGROUND_A);
		} else if ("1".equals(colorStr)) {
			Statistics.onEvent(GuideGalleryActivity.this, Statistics.NOTE_BACKGROUND_B);
		} else if ("2".equals(colorStr)) {
			Statistics.onEvent(GuideGalleryActivity.this, Statistics.NOTE_BACKGROUND_C);
		} else if ("3".equals(colorStr)) {
			Statistics.onEvent(GuideGalleryActivity.this, Statistics.NOTE_BACKGROUND_D);
		}
	}
	
	private void selectBg(){
		// gn pengwei 20121126 add for statistics begin
		String colorStr = bgColor + "";
		judgeColor(colorStr);
		// gn pengwei 20121126 add for statistics end
	}
	
}
