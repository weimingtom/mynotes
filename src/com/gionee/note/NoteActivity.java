package com.gionee.note;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import amigo.app.AmigoActionBar;
import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import amigo.app.AmigoProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ImageSpan;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import amigo.widget.AmigoButton;
import amigo.widget.AmigoEditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;

import com.gionee.note.NoteEditText.IOnKeyboardStateChangedListener;
import com.gionee.note.NoteEditText.OnTextViewChangeListener;
import com.gionee.note.adapter.NotePagerAdapter;
import com.gionee.note.adapter.NotePagerAdapter.IOnDownListener;
import com.gionee.note.content.Constants;
import com.gionee.note.content.NoteApplication;
import com.gionee.note.content.Notes;
import com.gionee.note.content.ResourceParser;
import com.gionee.note.content.ResourceParser.NoteBgResources;
import com.gionee.note.content.Session;
import com.gionee.note.content.StatisticalName;
import com.gionee.note.content.StatisticalValue;
import com.gionee.note.database.DBOpenHelper;
import com.gionee.note.database.DBOperations;
import com.gionee.note.domain.MediaInfo;
import com.gionee.note.domain.Note;
import com.gionee.note.domain.NoteLocation;
import com.gionee.note.noteMedia.location.GNLocateService2.GNLocationListener;
import com.gionee.note.noteMedia.location.GNLocateService2;
import com.gionee.note.noteMedia.location.GnSelectPoiActivity;
import com.gionee.note.noteMedia.record.NoteMediaManager;
import com.gionee.note.utils.AlarmUtils;
import com.gionee.note.utils.CommonUtils;
import com.gionee.note.utils.FileUtils;
import com.gionee.note.utils.Log;
import com.gionee.note.utils.PlatForm;
import com.gionee.note.utils.Statistics;
import com.gionee.note.utils.UtilsQueryDatas;
import com.gionee.note.utils.WidgetUtils;
import com.gionee.note.widget.NoteWidgetProvider;
import com.gionee.note.R;

import android.graphics.drawable.ColorDrawable; 
import android.text.Spanned;
import android.graphics.drawable.Drawable;

/*
 * note detail
 */
//Gionee <pengwei><20130823> modify for CR00869441 begin
public class NoteActivity extends AmigoActivity implements OnGestureListener{
	private  CharSequence[] mMenuItem;

	private static int TITLE_MAX_LENGTH = 60;
	// GN jiating CR00687672 limit input words begin
	private static int CONTENT_MAX_LENGTH = 5000;
	// GN jiating CR00687672 limit input words end

	private DBOperations dbo;

	private NoteEditText noteEditText;

	private int widgetId;
	private int widgetType;
	private Note note;

	private Intent intent;
	private int _id;
	private int folderId;

	// gn lilg 20120523 set font size
	private Integer noteFontSize = 12;

	private static final int SHOWVIEWPANEL = 0;
	private static final int REFLASHUI = SHOWVIEWPANEL + 1;

	private static final int GUIDEGALLERYACTIVITYCODE = 0;
	public static final int GUIDEGALLERYACTIVITYRESULTOK = 1;
	public static final int GUIDEGALLERYACTIVITYRESULTCANCEL = 2;


	private Handler handler;
	private boolean save2DB = true;
	// private ImageView menuView;
	private GestureDetector detector;
	private List<Note> allNodesInFolder;
	private boolean responseGesture;
	private int showPage;
	private TextView showNoteContent;
	private View editTextLayout;

	private Animation titleShowAnimation;
	private Animation titleHideAnimation;

	// hxc add for location start
	private boolean hasLocation = false;
	private String mLastAddress = "";
	private ImageView mBtnAlarm;
	private ImageView mBtnAddress;
	private ImageView mBtnAttachment;
	private PopupWindow mAlarmPopupWindow;
	private PopupWindow mRecordPopupWindow;
	private PopupWindow mLocationPopupWindow;
	private PopupWindow mAttachmentPopupWindow;
	private TextView mCustomView;
	private static final int REQUEST_CODE_LOCATION_POI = 3;
	public static final int RESULT_CODE_LOCATION_POI_OK = 4;
	private static final int REQUEST_CODE_LOCATION_TIP = 5;
	public static final int RESULT_CODE_LOCATION_TIP_OK = 6;
	// Gionee 20121031 jiating begin

	// Gionee <lilg><2013-04-02> modify for location with google api begin
	// gn lilg 2013-01-07 add for location begin
//	private GNLocateService mGNLocateService;
	private GNLocateService2 mGNLocateService2;
	private AmigoProgressDialog mProgressDialog;
	// gn lilg 2013-01-07 add for location end
	// Gionee <lilg><2013-04-02> modify for location with google api end

	// gn lilg 2013-01-18 add for location timeout begin
	private LocationTimeOutRunnable locTimeOutRunnable;
	private long locationStartTime;
	private static final long LOCATION_TIME_OUT_MILLISECONDS = 15000;
	// gn lilg 2013-01-18 add for location timeoutend

	// gn lilg 2013-02-19 add for media record begin
	private static final int ZERO_STORE = 0;
	private static final int MIN_AVAILABLE_STORE = 1;
	private static final int TYPE_MEDIA_RECORDER = 10;
	private static final int TYPE_MEDIA_PLAYER = 11;
	private static final int FLAG_UPDATE_MEDIA_CURRENT_TIME = 1001;
	private static final int FLAG_STOP_RECORDER_RECORDING = 1002;
	private static final int FLAG_ALERT_MESSAGE = 1003;
	private static final int FLAG_STOP_PLAYER_PLAYING = 1004;
	private static final int ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE = 1005;
	private static final int ERROR_SDCARD_MIN_AVAILABLE_STORE = 1006;
	private static final int SUCCESS_SDCARD_STATE = 1007;
	private static final int ERROR_INTERNAL_MEMORY_NOT_EXISTS_OR_UNAVAILABLE = 1008;
	private static final int ERROR_INTERNAL_MEMORY_MIN_AVAILABLE_STORE = 1009;
	private static final int SUCCESS_INTERNAL_MEMORY_STATE = 1010;

	public static final int ERROR_RECORDER_RECORDING = 1101;
	public static final int ERROR_PLAYER_PLAYING = 1102;
	public static final int SUCCESS_PLAYER_PLAYING = 1103;
	public static final int FLAG_START_MEDIA_RECORDER = 1104;
	public static final int FLAG_START_MEDIA_PLAYER = 1105;
	public static final int FLAG_DELETE_MEDIA_RECORD = 1106;

	private LinearLayout mLayoutMediaRecorder;
	private TextView mCurrentRecordTime;
	private ImageButton mBtnStopRecord;
	private LinearLayout mLayoutMediaPlayer;
	private TextView mCurrentPlayTime;
	private ImageButton mBtnStopPlay;

	private NoteMediaManager mMediaManager;
	private Timer mMediaTimer;
	private int mMediaMinute;
	private int mMediaSecond;
	public static final int defaultColor = 0;
	public static final int progressTime = 200;
	private Map<Integer, Integer> noteEditTextPosMap; // key is tag position,
														// value is type, type
														// == 0 is prefix, type
														// == 1 is suffix
	// gn lilg 2013-02-19 add for media record end

	// Gionee <lilg><2013-03-09> add for media progress bar begin
	private ProgressBar mProgressBarMediaPlayer;
	// Gionee <lilg><2013-03-09> add for media progress bar end
	private Handler homeProgressHandler;
	private AmigoProgressDialog mProDialog;
	private boolean startDialog = false;
	// Gionee <lilg><2013-03-27> add for NT028 begin
	private ClipboardManager mClipboardManager;
	private OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
	// Gionee <lilg><2013-03-27> add for NT028 end
	
	private LinearLayout mBtNoteVoice;
	private LinearLayout mBtNoteLocation;
	private LinearLayout mBtNoteAlarm;
    private MediaImageHandler mMediaImageHandler;

	private BroadcastReceiver InstallWidgetResultReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Log.i("NoteActivity------installWidger");
			// TODO Auto-generated method stub
			if (!Constants.SEND_INSTALL_WIDGET_RESULT
					.equals(intent.getAction())) {
				return;
			}
			Bundle data = intent.getExtras();
			if (data == null) {
				return;
			}
			String requestFlat = data
					.getString(Constants.KEY_RESULT_REQUEST_FLAG);
			Log.i("NoteActivity------installWidger, requestFlat: "
					+ requestFlat);
			Log.i("NoteActivity------installWidger, note.getId(): "
					+ note.getId());
			if (requestFlat.equals(note.getId())) {
				int resultFlag = data.getInt(Constants.KEY_RESULT_FALG);
				Log.d("NoteActivity------installWidger, resultFlag: "
						+ resultFlag);
				if (Constants.FLAG_INSTALL_WIDGET_SUCCESS == resultFlag) {
					widgetId = data.getInt(Constants.KEY_RESULT_WIDGET_ID);
					widgetType = Notes.TYPE_WIDGET_2X;
					note.setWidgetId(String.valueOf(widgetId));
					note.setWidgetType(String.valueOf(Notes.TYPE_WIDGET_2X));
					dbo.updateNote(NoteActivity.this, note);
					// GN pengwei 2012-11-26 add for CR00751323 end
					// GN pengwei 2012-11-08 add for CR00725641 end
					Log.d("NoteActivity------installWidger, widgetId: "
							+ widgetId);
					CommonUtils.showToast(
							context,
							getResources().getString(
									R.string.note_send_luncher_success_toast));
				} else if (Constants.FLAG_SCREEN_FULL == resultFlag) {
					CommonUtils
							.showToast(
									context,
									getResources()
											.getString(
													R.string.note_send_luncher_failed_noscence_toast));
				} else if (Constants.FLAG_BIND_WIDGET_FAIL == resultFlag
						|| Constants.FLAG_OTHER == resultFlag) {
					CommonUtils.showToast(
							context,
							getResources().getString(
									R.string.note_send_luncher_failed_toast));
				}
			}
		}
	};

	// GN pengwei 2012-12-19 Commented out for CR00751323 begin
	// Gionee 20121031 jiating end
	// hxc add for location end
	//
	// private BroadcastReceiver alarmReceiver=new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// // TODO Auto-generated method stub
	// Log.i("NoteActivity------alarmReceiver");
	// if (_id != -1) {
	// Log.i("NoteActivity------alarmReceiver+_id======" + _id);
	// note = dbo.queryOneNote(NoteActivity.this, _id);
	// folderId = "no".equals(note.getParentFile()) ? -1 : Integer
	// .parseInt(note.getParentFile());
	// }
	// Log.i("NoteActivity------alarmReceiver, note.getAlarm: "+note.getAlarmTime());
	// // updateView();
	// }
	// };
	// GN pengwei 2012-12-19 Commented out for CR00751323 end
	private int enterIntoEdit;
	private int NOTE_CHANGE_BG = 0;
	private int NOTE_SEND_TO_LAUNCH = 1;
	private int NOTE_SHARE = 2;
	private int NOTE_DEL = 3;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("GN_note---NoteActivity------onCreate---" + new Date());
		CommonUtils.setTheme(this);
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		registerSDListener();
		// Gionee 20121031 jiating begin
		IntentFilter widgetFilter = new IntentFilter(
				Constants.RECEIVER_WIDGET_REVEIVER_ACTION);
		registerReceiver(InstallWidgetResultReceiver, widgetFilter);
		// Gionee 20121031 jiating end
		IntentFilter noteFilter = new IntentFilter(
				CommonUtils.NOTEACTIVITY_REFRESH);
		registerReceiver(noteRefreshReceiver, noteFilter);
		
		IntentFilter alarmRefFilter = new IntentFilter(CommonUtils.ALARMREFRESH);
		registerReceiver(alarmRefreshReceiver, alarmRefFilter);
		
		setContentView(R.layout.note_detail_white);
		initActionBar();
		Statistics.setReportCaughtExceptions(true);
		// gn lilg 2012-12-08 modify for optimization begin
		dbo = DBOperations.getInstances(NoteActivity.this);
		// gn lilg 2012-12-08 modify for optimization end

		detector = new GestureDetector(this, this);
		allNodesInFolder = new ArrayList<Note>();

		intent = getIntent();

		// GN pengwei 2012-11-22 modify for find bugs start
		if (intent == null) {
			// GN pengwei 2012-11-22 modify for find bugs end
			startActivity(new Intent(NoteActivity.this, HomeActivity.class));
		}
		_id = intent.getIntExtra(DBOpenHelper.ID, -1);
		folderId = intent.getIntExtra(DBOpenHelper.PARENT_FOLDER, -1);
//		getNoteID(getCurrentExist());
		enterIntoEdit = intent.getIntExtra(UtilsQueryDatas.enterIntoEditStr,-1);
		// gn pengwei 20130225 add for CR00772372 begin
		dealWithNoteId();
		// gn pengwei 20130225 add for CR00772372 end
		widgetId = intent.getExtras().getInt(NoteWidgetProvider.WIDGET_ID, -1);
		widgetType = intent.getExtras().getInt(NoteWidgetProvider.WIDGET_TYPE,
				-1);
		titleShowAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
				-1, Animation.RELATIVE_TO_SELF, 0);
		titleShowAnimation.setDuration(500);

		// titleShowAnimation.setInterpolator(AnimationUtils
		// .loadInterpolator(NoteActivity.this,
		// android.R.anim.accelerate_decelerate_interpolator));

		titleHideAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
				0, Animation.RELATIVE_TO_SELF, -1);
		titleHideAnimation.setDuration(500);

		// titleHideAnimation.setInterpolator(AnimationUtils
		// .loadInterpolator(NoteActivity.this,
		// android.R.anim.accelerate_decelerate_interpolator));

		// Gionee <lilg><2013-03-20> add for CR00784203 begin
		Session session = new Session();
		session.setScreenSize(NoteActivity.this);
		// Gionee <lilg><2013-03-20> add for CR00784203 end		
//        setOptionsMenuHideMode(true);
		initViews();
		initMenu();
		if (note != null) {
			CommonUtils.setNoteID(note.getId());
		}
		// Gionee <lilg><2013-04-02> modify for location with google api begin
		// gn lilg 2013-01-07 add for location begin
//		mGNLocateService = GNLocateService.getInstance(getApplicationContext());
		mGNLocateService2 = GNLocateService2.getInstance(getApplicationContext());
		setLocationListener();
		// gn lilg 2013-01-07 add for location end
		// Gionee <lilg><2013-04-02> modify for location with google api end

		// gn lilg 2013-02-19 add for media record begin
		mMediaManager = NoteMediaManager.getInstances(getApplicationContext(),
				handler);
		// gn lilg 2013-02-19 add for media record end

		// Gionee <lilg><2013-03-27> add for NT028 begin
		mOnPrimaryClipChangedListener = new OnPrimaryClipChangedListener(){
			@Override
			public void onPrimaryClipChanged() {
				Log.i("NoteActivity------on primary clip changed!");
				
				ClipData clipData = mClipboardManager.getPrimaryClip();
				
				if(clipData != null && clipData.getItemCount() > 0){
					Item item = clipData.getItemAt(0);
					String text = String.valueOf(item.getText());
					Log.d("NoteActivity------clip text: " + text);
					
					if(!TextUtils.isEmpty(text) && text.contains(NoteMediaManager.TAG_PREFIX) && text.contains(NoteMediaManager.TAG_PARSE_SUFFIX)){
						String textFiltered = CommonUtils.noteContentPreDeal(text);
						Log.d("NoteActivity------text filtered: " + textFiltered);
						
						if(textFiltered != null){
							ClipData clip = ClipData.newPlainText(Constants.PACKAGE_NAME, textFiltered);
							mClipboardManager.setPrimaryClip(clip);
						}
					}
				}
			}
		};
		
		mClipboardManager =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		// Gionee <lilg><2013-03-27> add for NT028 end
		
		Log.i("GN_note---NoteActivity------onCreate---finish----" + new Date());
		if(HomeActivity.mTempNoteList == null || UtilsQueryDatas.enterIntoEdit == enterIntoEdit){
		    enterIntoEdit(note);
		}
		Log.d("GN_note---NoteActivity------onCreate---notePage----" + notePage);
	}

    private void initMenu(){
		String deviceModel = android.os.Build.MODEL; // phone model
		Log.v("NoteActivity---onCreateOptionsMenu---deviceModel == "
				+ deviceModel);
		Log.v("NoteActivity---onCreateOptionsMenu---deviceModel.indexOf(DEVICE_MODEL) == " + deviceModel.indexOf(DEVICE_MODEL) );
		Log.v("NoteActivity---onCreateOptionsMenu---responseGesture == " + responseGesture);
		// Gionee <wangpan> <2014-01-02> modify for CR00972801 begin
		//if (DEVICE_MODEL.indexOf(deviceModel) >= 0) {// Wiget does not support
        Log.v("NoteActivity---onCreateOptionsMenu---isNaviLauncher() == " + isNaviLauncher());
		if (isNaviLauncher() || !isSupportSendToDesk()) {// Wiget does not support
		// Gionee <wangpan> <2014-01-02> modify for CR00972801 end
														// E6, so
            // Gionee <wangpan><2014-03-24> add for CR01133348 begin
			// get rid of
			/*if (!responseGesture) {*/
				mMenuItem = new CharSequence[] {
						this.getResources().getString(
								R.string.note_change_bgcolor),
						this.getResources().getString(R.string.share),
						this.getResources().getString(R.string.delete) };
				setMenuThreeNoSendToLaunchItems();
				Log.v("NoteActivity---onCreateOptionsMenu---1");
			/*} else {
				mMenuItem = new CharSequence[] {
						this.getResources().getString(R.string.share),
						this.getResources().getString(R.string.delete) };
				setMenuTwoItems();
				Log.v("NoteActivity---onCreateOptionsMenu---2");

			}*/
            // Gionee <wangpan><2014-03-24> add for CR01133348 end
		} else {
			if (!responseGesture) {
				mMenuItem = new CharSequence[] {
						this.getResources().getString(
								R.string.note_change_bgcolor),
						this.getResources().getString(
								R.string.note_send_luncher),
						this.getResources().getString(R.string.share),
						this.getResources().getString(R.string.delete) };
				setMenuFourItems();
				Log.v("NoteActivity---onCreateOptionsMenu---3");

			} else {
				mMenuItem = new CharSequence[] {
						this.getResources().getString(
								R.string.note_send_luncher),
						this.getResources().getString(R.string.share),
						this.getResources().getString(R.string.delete) };
				setMenuThreeNoChangeBgItems();
				Log.v("NoteActivity---onCreateOptionsMenu---4");

			}
		}
	}
	// Gionee <wangpan> <2014-01-14> add for CR00986147 begin
	private boolean isSupportSendToDesk() {
	    String isSupportSendToDesk = SystemProperties.get("ro.gn.note.desktop.support","no");
        if ("yes".equals(isSupportSendToDesk)) {
            return true;
        } else {
            return false;
        }
    }
    // Gionee <wangpan> <2014-01-14> add for CR00986147 end

    private void setMenuFourItems(){
    	NOTE_CHANGE_BG = 0;
    	NOTE_SEND_TO_LAUNCH = 1;
    	NOTE_SHARE = 2;
    	NOTE_DEL = 3;
	}
	
	private void setMenuThreeNoChangeBgItems(){
    	NOTE_CHANGE_BG = -1;
    	NOTE_SEND_TO_LAUNCH = 0;
    	NOTE_SHARE = 1;
    	NOTE_DEL = 2;
	}
	
	private void setMenuThreeNoSendToLaunchItems(){
    	NOTE_CHANGE_BG = 0;
       	NOTE_SEND_TO_LAUNCH = -1;
    	NOTE_SHARE = 1;
    	NOTE_DEL = 2;
	}
	
	private void setMenuTwoItems(){
    	NOTE_CHANGE_BG = -1;
    	NOTE_SEND_TO_LAUNCH = -1;
    	NOTE_SHARE = 0;
    	NOTE_DEL = 1;
	}
	
	private void setLocationListener() {
		// Gionee <lilg><2013-04-02> modify for location with google api begin
//		if (mGNLocateService != null) {
//			mGNLocateService.setGNLocationListener(new GNLocationListener() {
		if (mGNLocateService2 != null) {
			mGNLocateService2.setGNLocationListener(new GNLocationListener() {
				// Gionee <lilg><2013-04-02> modify for location with google api end
				@Override
				public void onLocatePoi(ArrayList<NoteLocation> poi, int temp) {
					Log.d("NoteActivity------onLocatePoi!");

					onDismissDialog();
					// gn lilg 2013-01-18 add for location timeout begin
					if (handler != null) {
						handler.removeCallbacks(locTimeOutRunnable);
					}
					// gn lilg 2013-01-18 add for location timeout end
					if (poi == null || poi.size() <= 0) {
//						Toast.makeText(NoteActivity.this, getResources().getString(R.string.gn_location_error),	Toast.LENGTH_SHORT).show();
						if(handler != null){
							Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
							message.obj = getResources().getString(R.string.gn_location_error);
							message.sendToTarget();
						}
					} else {
						Intent i = new Intent(NoteActivity.this,
								GnSelectPoiActivity.class);
						i.putExtra("poi", poi);
						startActivityForResult(i, REQUEST_CODE_LOCATION_POI);
					}
				}

				@Override
				public void onLocatePoi(ArrayList<String> poi) {
					// TODO Auto-generated method stub
				}
				
				// Gionee <lilg><2013-04-02> modify for location with google api begin
				@Override
				public void onProviderDisabled() {
					Log.d("NoteActivity------onProviderDisabled!");
					
					onDismissDialog();
					if (handler != null) {
						handler.removeCallbacks(locTimeOutRunnable);
					}
					Toast.makeText(NoteActivity.this, getResources().getString(R.string.gn_location_no_setting), Toast.LENGTH_LONG).show();
				}
				// Gionee <lilg><2013-04-02> modify for location with google api end
			});
		}
	}

	//Gionee <pengwei><20130702> modify for CR00832081 begin
	private BroadcastReceiver mSDReceiver = null;
	private void registerSDListener(){
	  mSDReceiver=new BroadcastReceiver(){
			  
			  @Override
			   public void onReceive(Context context, Intent intent) {
			   // TODO Auto-generated method stub
				  String action = intent.getAction();
                  if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                	  activityFinish();
                  }			   
                 }
			            
			};
        IntentFilter filter=new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mSDReceiver,filter);
	}
	
	private void activityFinish(){
		this.finish();
	}
	//Gionee <pengwei><20130702> modify for CR00832081 end
	
	// gn pengwei 20121214 add for Common control begin
	private AmigoActionBar actionbar;
	private ImageButton noteAttchmentItem;
	private ImageButton noteMenu;
	private void initActionBar() {
		actionbar = this.getAmigoActionBar();
		// Gionee <lilg><2013-05-24> modify for CR00809680 begin
		// actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.home_title_bg_white));
		// Gionee <lilg><2013-05-24> modify for CR00809680 end
		actionbar.setIcon(R.drawable.gn_note_actionbar_icon);
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setDisplayShowCustomEnabled(true);
		mActionBarView = LayoutInflater.from(actionbar.getThemedContext())
				.inflate(R.layout.note_action_bar_custom, null);
		actionbar.setCustomView(mActionBarView, new AmigoActionBar.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mCustomView = (TextView) mActionBarView
				.findViewById(R.id.actionbar_note_title);
		noteAttchmentItem = (ImageButton) mActionBarView
				.findViewById(R.id.actionbar_note_attachment);
		noteAttchmentItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("GN_note---onClick---");
				clickAttachment();
			}
		});
		
		noteMenu = (ImageButton) mActionBarView
				.findViewById(R.id.actionbar_note_menu_view);
		noteMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(CommonUtils.isFastDoubleClick()){
					return;
				}
				menuOnClick();
			}
		});
	}

	//Gionee <pengwei><20131010> modify for CR00916609 begin
 	private void initMenuAndAttachment(){
 	    if (responseGesture) {
 	        showMenuItem();
 	        noteAttchmentItem.setImageResource(R.drawable.note_attachment_disable);
 	        isResponseGesture(false);
 	    }else{
 	        if (noteEditText.getText().length() < 1) {
 	            // gionee 20121204 jiating modify for CR00739157 begin
 	            // noteShare.setVisible(false);
 	            CommonUtils.isAbledAdd(this, createNoteItem, false);
 	            // gionee 20121204 jiating modify for CR00739157 end
 	        } else {
 	            CommonUtils.isAbledAdd(this, createNoteItem, true);			      }
 	    }
	}
	//Gionee <pengwei><20131010> modify for CR00916609 end
 	
	// gn pengwei 20121214 add for Common control end

	// gn pengwei 20130225 add for CR00772372 begin
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		dealWithNoteIdHome();
		Log.d("GN_note---NoteActivity------onRestart---" + new Date());
	}

	// gn pengwei 20130225 add for CR00772372 end

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("GN_note---NoteActivity------onResume---" + new Date());
		
		// Gionee <lilg><2013-04-10> add for note upgrade begin
		((NoteApplication) getApplication()).registerVersionCallback(this);
		// Gionee <lilg><2013-04-10> add for note upgrade end
		
		// gn jiating 20121009 GN_GUEST_MODE begin
		// gn pengwei 20130122 modify for CR00766723 begin
		// gn pengwei 20130227 modify for CR00774184 begin
		if (NoteApplication.GN_GUEST_MODE && _id != -1) {
			// gn pengwei 20130227 modify for CR00774184 end
			HomeActivity.setInFolder(false);
			Intent intent = new Intent(NoteActivity.this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		// gn pengwei 20130122 modify for CR00766723 end
		// gn jiating 20121009 GN_GUEST_MODE end
		initNoteScreen(note);
		Log.d("GN_note---NoteActivity------onResume---finish---" + new Date());
		Statistics.onResume(this);
	}

	private void initNoteScreen(Note note) {
	    Log.d("initNoteScreen-bgcolor: "+note.getBgColor());
		try {
		    if (note.getBgColor() != null) {
		        noteTabRel.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(Integer.parseInt(note
		                        .getBgColor())));
		        contentLinear.setBackgroundResource(NoteBgResources
		                .getNoteBgResourceWhite(Integer.parseInt(note
		                        .getBgColor())));
		        noteBottom.setBackgroundResource(NoteBgResources
		                .getNoteBgBottomResourceWhite(Integer.parseInt(note
		                        .getBgColor())));
		        mLayoutMediaRecorder.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(Integer.parseInt(note
		                        .getBgColor())));
		        mLayoutMediaPlayer.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(Integer.parseInt(note
		                        .getBgColor())));
		    } else {
		        noteTabRel.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(defaultColor));
		        contentLinear.setBackgroundResource(NoteBgResources
		                .getNoteBgResourceWhite(defaultColor));
		        noteBottom.setBackgroundResource(NoteBgResources
		                .getNoteBgBottomResourceWhite(defaultColor));
		        mLayoutMediaRecorder.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(defaultColor));
		        mLayoutMediaPlayer.setBackgroundResource(NoteBgResources
		                .getNoteBgTopWhite(defaultColor));
		    }
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("NoteActivity---initNoteScreen---" + e);
		}

	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("GN_note---NoteActivity------onActivityResult------GUIDEGALLERYACTIVITYRESULTOK---"
				+ GUIDEGALLERYACTIVITYRESULTOK);
		if (requestCode == GUIDEGALLERYACTIVITYCODE
				&& resultCode == GUIDEGALLERYACTIVITYRESULTOK) {
			note.setBgColor(String.valueOf(data.getIntExtra("bgColor", 0)));

			note.setUpdateDate(dbo.getDate());
			note.setUpdateTime(dbo.getTime());

			dbo.updateNote(NoteActivity.this, note);

			initNoteScreen(note);
			changeTextColor(note);
            // Gionee <wangpan><2014-03-24> add for CR01133348 begin
            if(responseGesture){
                UtilsQueryDatas.changeNote(note, HomeActivity.mTempNoteList);
                UtilsQueryDatas.changeNote(note, mNoteDatas);
                browseView.setCurrentItem(mNoteDatas.indexOf(note), true);
                //浏览模式，才更新数据
                notePageAdpter.notifyDataSetChanged();
                WidgetUtils.updateWidget(this,
                        Integer.parseInt(note.getWidgetId()),
                        Integer.parseInt(note.getWidgetType()));
            }
            // Gionee <wangpan><2014-03-24> add for CR01133348 end
		}
		// hxc add for location start
		if (requestCode == REQUEST_CODE_LOCATION_POI
				&& RESULT_CODE_LOCATION_POI_OK == resultCode && null != data) {
			Log.i("GN_note---NoteActivity------onActivityResult------mLocation---"
					+ new Date());
			// gn lilg 2013-01-07 modify for location begin
			noteEditText.requestFocus();
			String name = data.getStringExtra("poi");
			Log.d("NoteActivity------address select: " + name);
			setLocationResult(name);
			// gn lilg 2013-01-07 modify for location end
		}
		if (requestCode == REQUEST_CODE_LOCATION_TIP
				&& RESULT_CODE_LOCATION_TIP_OK == resultCode) {
			Log.d("NoteActivity------requestCode: " + requestCode
					+ ", resultCode: " + resultCode);
		}
		// hxc add for location end
	}

	private RelativeLayout noteTabRel = null;
	private RelativeLayout contentLinear = null;
	private TextView timeShow = null;
	private RelativeLayout labelAlarm;
	private TextView alarmText;
	private ImageView alarmBtn;
	private RelativeLayout labelAddress;
	private TextView addressText;
	private ImageView addressBtn;
	private LinearLayout alarmContext;
	private LinearLayout addressContext;
	private ImageView noteVoice;
	private ImageView createNoteItem;
	private LinearLayout noteBottom;
	private View labelDivision;
	private ProgressBar mProgressBarMediaRecord;
	private int notePage = 1;
	private LinearLayout noteTopEdit;
	private List<Note> mNoteDatas = null; 
	private ViewPager browseView;
	//Gionee <pengwei><20130826> modify for CR00873479 begin
	private void initViews() {
		Log.i("NoteActivity------initViews---" + new Date());
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFLASHUI:
					// gn pengwei 20130109 modify begin
					if (note == null) {
						Log.v("GN_Note---NoteActivity---initViews---handler---note---null---this is null");
						return;
					}
					// gn pengwei 20130109 modify end
					initNoteScreen(note);

					// gn lilg 2013-02-27 modify for note content pre deal begin
					// noteEditText.setText(note.getContent());
					setTextForNoteEditText(note.getContent().trim());
					// showNoteContent.setText(note.getContent());
					setTextForTextView(note.getContent().trim());
					// gn lilg 2013-02-27 modify for note content pre deal end

					String title = note.getTitle() != null ? note.getTitle()
							: getResources().getString(
									R.string.note_new_title_label);
                    //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	                //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
					//mCustomView.setText(CommonUtils.getTitleString(title));
                     mCustomView.setText(getTitleString(title));
	                //Gionee <wangpan><2014-05-21> modify for CR01268739 end
                    //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
					if (note.getAlarmTime() == null
							|| Constants.INIT_ALARM_TIME.equals(note
									.getAlarmTime())) {
						hideAlarm();
					} else {
						showAlarm(note);
					}

					if ((note.getAddressName() != null && !"".equals(note
							.getAddressName()))) {
						showAdress(note.getAddressName());
					} else {
						hideAddress();
					}
					String timeShowStr = CommonUtils.getNoteData(
							NoteActivity.this, note.getUpdateDate(),
							note.getUpdateTime());
					timeShow.setText(timeShowStr);
					changeTextColor(note);
					Log.v("GN_Note---NoteActivity---initViews---handler---note.getTitle()---"
							+ note.getTitle()
							+ "---note.getId()"
							+ note.getId());
					Log.v("GN_Note---NoteActivity---initViews---handler---note.getContent()---"
							+ note.getContent()
							+ "---note.getId()"
							+ note.getId());
					Log.v("GN_Note---NoteActivity---initViews---handler---note.getUpdateTime()---"
							+ note.getUpdateTime()
							+ "---note.getId()"
							+ note.getId());
					Log.v("GN_Note---NoteActivity---initViews---handler---noteEditText---"
							+ noteEditText.getText()
							+ "---note.getId()"
							+ note.getId());
					Log.v("GN_Note---NoteActivity---initViews---handler---showNoteContent---"
							+ showNoteContent.getText()
							+ "---note.getId()"
							+ note.getId());
					Log.v("GN_Note---NoteActivity---initViews---handler---mCustomView---"
							+ mCustomView.getText()
							+ "---note.getId()"
							+ note.getId());
					break;
				case FLAG_UPDATE_MEDIA_CURRENT_TIME:

					// gn lilg 2013-01-19 add for update recorder current time
					// begin
					// update the record time while recording
					String info = (String) msg.obj;
					if (!TextUtils.isEmpty(info)
							&& msg.arg1 == TYPE_MEDIA_RECORDER) {
						mCurrentRecordTime.setText(info);
					} else if (!TextUtils.isEmpty(info)
							&& msg.arg1 == TYPE_MEDIA_PLAYER) {
						mCurrentPlayTime.setText(info);

						// Gionee <lilg><2013-03-11> add for update the player
						// current progress begin
						int currProgress = msg.arg2;
						mProgressBarMediaPlayer.setProgress(currProgress);
						// Gionee <lilg><2013-03-11> add for update the player
						// current progress end
					}
					// gn lilg 2013-01-19 add for update recorder current time
					// end

					break;
				case FLAG_START_MEDIA_RECORDER:

					// gn lilg 2013-02-22 add for handle to start recorder begin
					startMediaRecording();
					// gn lilg 2013-02-22 add for handle to start recorder end

					break;

					case FLAG_START_MEDIA_PLAYER:

					break;
				case ERROR_RECORDER_RECORDING:
				case FLAG_STOP_RECORDER_RECORDING:

					// gn lilg 2013-01-20 add for handle recorder excpetion
					// begin
					stopMediaRecording(save2DB);
					// gn lilg 2013-01-20 add for handle recorder excpetion end

					break;
				case SUCCESS_PLAYER_PLAYING:
				case ERROR_PLAYER_PLAYING:
				case FLAG_STOP_PLAYER_PLAYING:

					// gn lilg 2013-01-20 add for handle recorder excpetion
					// begin
					stopMediaPlaying();
					// gn lilg 2013-01-20 add for handle recorder excpetion end

					break;
				case FLAG_ALERT_MESSAGE:

					// gn lilg 2013-01-20 add for handle alert message begin
					String alertInfo = (String) msg.obj;
					if (!TextUtils.isEmpty(alertInfo)) {
						Toast.makeText(NoteActivity.this, alertInfo,
								Toast.LENGTH_SHORT).show();
						attachmentEnabled();
					}
					// gn lilg 2013-01-20 add for handle alert message end
					break;
				case FLAG_DELETE_MEDIA_RECORD:

					Toast.makeText(NoteActivity.this, "delete",
							Toast.LENGTH_SHORT).show();

					break;
				}

			}
		};
		noteTopEdit = (LinearLayout) findViewById(R.id.note_top_edit_lyt);
		timeShow = (TextView) findViewById(R.id.note_time_show);
		noteTabRel = (RelativeLayout) findViewById(R.id.note_tab);
		contentLinear = (RelativeLayout) findViewById(R.id.content_linear);
		noteBottom = (LinearLayout) findViewById(R.id.note_detail_bottom);
		// gn lilg 20120523 note font size
		editTextLayout = findViewById(R.id.edit_text_layout);
		showNoteContent = (TextView) findViewById(R.id.note_show_content);
		noteEditText = (NoteEditText) findViewById(R.id.et_content);
		// gn lilg 2013-02-22 add for media recorder can be clicked in the
		// editText begin
		// noteEditText.setMovementMethod(LinkMovementMethod.getInstance());
		// gn lilg 2013-02-22 add for media recorder can be clicked in the
		// editText end
		noteEditText
				.setOnKeyboardStateChangedListener(new IOnKeyboardStateChangedListener() {

					public void onKeyboardStateChanged(int state) {
						switch (state) {
						case NoteEditText.KEYBOARD_STATE_HIDE:// 软键盘隐藏
							// noteBottom.setVisibility(View.VISIBLE);
							break;
						case NoteEditText.KEYBOARD_STATE_SHOW:// 软键盘显示
							// noteBottom.setVisibility(View.GONE);
							break;
						default:
							break;
						}
					}
				});
		mBtnAttachment = (ImageView) findViewById(R.id.add_attachment_button);
		createNoteItem = (ImageView) findViewById(R.id.create_new_note);
		createNoteItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				handler.removeMessages(SHOWVIEWPANEL);
				// gn pengwei 20121126 add for statistics begin
				Statistics.onEvent(NoteActivity.this, Statistics.NOTE_ADDNOTE);
				// gn pengwei 20121126 add for statistics end
				Intent i = new Intent();
				i.putExtra(DBOpenHelper.PARENT_FOLDER, folderId);
				i.setClass(NoteActivity.this, NoteActivity.class);
				startActivity(i);
				NoteActivity.this.finish();
			}
		});
//		showNoteContent
//				.setMovementMethod(ScrollingMovementMethod.getInstance());
		browseView = (ViewPager) findViewById(R.id.note_detail_browse_vp);
		labelDivision = (View) findViewById(R.id.label_division);
		labelAlarm = (RelativeLayout) findViewById(R.id.label_alarm);
		alarmContext = (LinearLayout) findViewById(R.id.label_alarm_context);
		alarmText = (TextView) findViewById(R.id.alarm_text);
		alarmContext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setAlarmTime();
			}
		});
		alarmBtn = (ImageView) findViewById(R.id.alarm_btn);
		alarmBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				deleteAlarm(note);
			}
		});
		labelAddress = (RelativeLayout) findViewById(R.id.label_address);
		addressText = (TextView) findViewById(R.id.address_text);
		addressBtn = (ImageView) findViewById(R.id.address_btn);
		addressContext = (LinearLayout) findViewById(R.id.label_address_context);
		addressContext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setAddress();
			}
		});
		addressBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				deleteAddress();
				hideAddress();
			}
		});
		mCustomView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				handler.removeMessages(SHOWVIEWPANEL);
				return false;
			}
		});

		mCustomView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showNoteNameDialog();
			}
		});

		// gn lilg 2013-02-26 add for deal media info when note edit text
		// changed begin
	    // Gionee <pengwei><2013-08-26> add for CR00873485 begin
        // Gionee <wangpan><2014-03-14> modify for CR01083606 begin
		mMediaImageHandler = new MediaImageHandler(noteEditText);
		/*
		noteEditText.addTextChangedListener(new TextWatcher() {
		    private String temp;// Temporary storage before the input string
            private int selectionStart;// the start position of the cursor
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("noteEditText.addTextChangedListener2: beforeTextChanged: "+s);
				
                temp = noteEditText.getText().toString();// Get before the input
                // string
                selectionStart = noteEditText.getSelectionStart();
                Log.i("temp: "+temp+"selectionStart: "+selectionStart);
                
                Log.i("noteEditText.addTextChangedListener2: beforeTextChanged:selectionStart: "+selectionStart);
				Log.d("NoteActivity------beforeTextChanged, CharSequence: " + s	+ ", start: " + start + ", count: " + count	+ ", after: " + after);

				// delete media info in the db, table MediaItems
				if (!responseGesture && s != null && after == 0) {
					CharSequence delCharSequence = s.subSequence(start,	((start + count) > s.length() ? s.length() : (start + count)));
					String delTextInfo = delCharSequence.toString();
					Log.d("NoteActivity------delete charSequence: "	+ delTextInfo);

					List<String> mediaInfoList = CommonUtils.getMediasFromNoteContent(delTextInfo);
					if(mediaInfoList != null && mediaInfoList.size() > 0){
						for(String mediaInfo : mediaInfoList){					
						Log.d("NoteActivity------delete mediaInfo: " + mediaInfo);
						if (!TextUtils.isEmpty(mediaInfo)) {
							String[] mediaInfos = mediaInfo.split(NoteMediaManager.TAG_MIDDLE);
							if (mediaInfos != null) {
								String mediaType = mediaInfos[0];

								String mediaFileName = mediaInfos[1];
								Log.d("NoteActivity------delete mediaInfo type: " + mediaType + ", mediaFileName: " + mediaFileName);

								// delete the media file in the sdcard
								Note tmpNote = DBOperations.getInstances(NoteActivity.this).queryOneNote(NoteActivity.this, Integer.parseInt(note.getId()));
//								String fileToDelete = tmpNote.getMediaFolderName() + "/" + mediaFileName;
								
								if(!TextUtils.isEmpty(tmpNote.getMediaFolderName())){
									
									// Gionee <lilg><2013-04-17> add for CR00795461 begin
//									String fileToDelete = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(tmpNote.getMediaFolderName()) + "/" + mediaFileName;
									String fileToDelete = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(NoteActivity.this, tmpNote.getMediaFolderName()) + "/" + mediaFileName;
									// Gionee <lilg><2013-04-17> add for CR00795461 end
									
									Log.d("NoteActivity------the media file to delete: " + fileToDelete);
									
									// the media in the backup dir will not to be delete.
									if(fileToDelete.contains(getResources().getString(R.string.path_note_media))){
										File mediaFile = new File(fileToDelete);
										if(mediaFile.exists()){
											boolean isDelete = mediaFile.delete();
											Statistics.onEvent(NoteActivity.this, Statistics.NOTE_DEL_RECORD);
											Log.d("NoteActivity------media file is delete: " + isDelete);
										}
									}
	
									// delete the media item in db
									DBOperations.getInstances(NoteActivity.this).deleteMediaByFileName(NoteActivity.this, tmpNote.getMediaFolderName() + "/" + mediaFileName);
									
									// stop playing
									if(mMediaManager != null && mMediaManager.isPlaying() && mMediaManager.getmMediaItemFileNameCurrentPlaying().equals(mediaFileName)){
										if(handler != null){
											handler.sendEmptyMessage(FLAG_STOP_PLAYER_PLAYING);
										}
									}
								}
							}
						  }
						}
					}
				}
			}
		    // Gionee <pengwei><2013-08-26> add for CR00873485 end
			// Gionee <pengwei><2013-09-30> modify for CR00909844 begin
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.d("NoteActivity------onTextChanged, CharSequence: " + s
						+ ", start: " + start + ", before: " + before
						+ ", count: " + count);
			}
			// Gionee <pengwei><2013-09-30> modify for CR00909844 end

			@Override
			public void afterTextChanged(Editable noteContent) {
                Log.d("noteEditText.addTextChangedListener2: afterTextChanged: "+noteContent);
                if (null != noteContent
                        && noteContent.length() > CONTENT_MAX_LENGTH) {
                    Toast.makeText(NoteActivity.this,
                            getString(R.string.note_content_max_length),
                            Toast.LENGTH_SHORT).show();
                    // The new join string into cursor back and prevent new
                    // input string cover in front of the string
                    int tempLen = temp.length();
                    CharSequence tempChar = noteContent.subSequence(
                            selectionStart,
                            selectionStart + noteContent.length() - tempLen);
                    CharSequence addChar = tempChar.subSequence(0,
                            CONTENT_MAX_LENGTH - tempLen);
                    String tempBef = temp.substring(0, selectionStart);
                    String tempAft = temp.substring(selectionStart, tempLen);
                    String showStr = tempBef + addChar + tempAft;
                    
                    // Gionee <lilg><2013-03-29> modify CR00791333 begin
//                  noteEditText.setText(showStr);
                    setTextForNoteEditText(showStr);
                    // Gionee <lilg><2013-03-29> modify CR00791333 end
                    // Gionee <wangpan><2014-11-20> modify for CR01412725 begin
                    noteEditText.setSelection(CONTENT_MAX_LENGTH);
                    // Gionee <wangpan><2014-11-20> modify for CR01412725 end
                }

                Log.v("NoteActivity--wpeng-----------" + noteContent);
                String sStr = "";
                if (noteContent != null) {
                    sStr = noteContent.toString();
                } 
                // Gionee <wangpan><2014-08-18> modify for CR01316171 begin 
                if ("".equals(sStr.trim())) {
                // Gionee <wangpan><2014-08-18> modify for CR01316171 end 
                    CommonUtils.isAbledAdd(NoteActivity.this, createNoteItem,
                            false);
                    CommonUtils.isAbledAdd(NoteActivity.this, shareItem, false);
                } else {
                    CommonUtils.isAbledAdd(NoteActivity.this, createNoteItem,
                            true);
                    CommonUtils.isAbledAdd(NoteActivity.this, shareItem, true);
                }
			}
		});
		*/
        // Gionee <wangpan><2014-03-14> modify for CR01083606 end
		// gn lilg 2013-02-26 add for deal media info when note edit text
		// changed end

		// Gionee <lilg><2013-03-08> add for play media info when media clicked
		// begin

		noteEditTextPosMap = new HashMap<Integer, Integer>();

		noteEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NoteActivity------on click note edit text!");

				// init noteEditTextPosMap
				String noteText = noteEditText.getText().toString();
				if (TextUtils.isEmpty(noteText)
						|| !noteText.contains(NoteMediaManager.TAG_PREFIX)
						|| !noteText
								.contains(NoteMediaManager.TAG_PARSE_SUFFIX)) {
					return;
				}

				initNoteEditTextPosMap(noteText);

				int selectionStart = noteEditText.getSelectionStart();
				int selectionEnd = noteEditText.getSelectionEnd();
				Log.d("NoteActivity------selection start: " + selectionStart
						+ ", selection end: " + selectionEnd);

				if (noteEditTextPosMap != null
						&& noteEditTextPosMap.containsKey(selectionStart)) {
					int posType = noteEditTextPosMap.get(selectionStart);
					Log.d("NoteActivity------posType: " + posType);

					// sub string media tag
					String mediaTag = "";
					//Gionee <pengwei><20130731> modify for CR00838996 begin
					try {
					if (posType == 0) {
						// selection pos at the media tag start
						mediaTag = noteText.substring(
								selectionStart,
								noteText.indexOf(
										NoteMediaManager.TAG_STORE_SUFFIX,
										selectionStart)
										+ NoteMediaManager.TAG_STORE_SUFFIX
												.length());
						Log.d("NoteActivity------mediaTag: " + mediaTag);
					} else if (posType == 1) {
						// selection pos at the media tag end
						String tmpMediaTag = noteText
								.substring(0, selectionEnd);
						mediaTag = tmpMediaTag.substring(tmpMediaTag
								.lastIndexOf(NoteMediaManager.TAG_PREFIX));
						Log.d("NoteActivity------mediaTag: " + mediaTag);
					}
					} catch (Exception e) {
						// TODO: handle exception
						Log.d("NoteActivity---onClick---e == " + e);
					}
					// start media player
					if (!TextUtils.isEmpty(mediaTag)
							&& mediaTag.startsWith(NoteMediaManager.TAG_PREFIX)
							&& (mediaTag
									.endsWith(NoteMediaManager.TAG_PARSE_SUFFIX) || mediaTag
									.endsWith(NoteMediaManager.TAG_STORE_SUFFIX))) {

						String mediaInfo = mediaTag.substring(
								NoteMediaManager.TAG_PREFIX.length(),
								mediaTag.indexOf(NoteMediaManager.TAG_PARSE_SUFFIX));
						Log.d("NoteActivity------mediaInfo: " + mediaInfo);

						String[] mediaInfoArray = mediaInfo
								.split(NoteMediaManager.TAG_MIDDLE);
						if (mediaInfoArray != null
								&& mediaInfoArray.length == 4) {
							
							// Gionee <lilg><2013-07-01> add for CR00830261 begin
							int tmpNoteId = -1;
							try{
								tmpNoteId = Integer.parseInt(note.getId());
							}catch(Exception e){
								Log.e("NoteActivity------parse note id: " + note.getId() + " exception." + e);
							}
							
							Log.d("NoteActivity------noteId: " + tmpNoteId);
							if(tmpNoteId == -1){
								return;
							}
							// Gionee <lilg><2013-07-01> add for CR00830261 end

							Note tmpNote = DBOperations.getInstances(NoteActivity.this).queryOneNote(NoteActivity.this, Integer.parseInt(note.getId()));
							tmpNote.setMediaFolderName(FileUtils.replaceTwoDivideToOne(tmpNote.getMediaFolderName()));
							Log.d("NoteActivity------note media folder name: " + tmpNote.getMediaFolderName() + ", media file name: " + mediaInfoArray[1]);
                            // Gionee <wangpan><2014-08-19> modify for CR01359336 begin
							if(!TextUtils.isEmpty(tmpNote.getMediaFolderName()) && !"-1".equals(tmpNote.getMediaFolderName())){
                            // Gionee <wangpan><2014-08-19> modify for CR01359336 end
	//							String mediaFileName = tmpNote.getMediaFolderName() + "/" + mediaInfoArray[1];
								
								// Gionee <lilg><2013-04-17> add for CR00795461 begin
//								String mediaFileName = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(tmpNote.getMediaFolderName()) + "/" + mediaInfoArray[1];
								String mediaFileName = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(NoteActivity.this, tmpNote.getMediaFolderName()) + "/" + mediaInfoArray[1];
								// Gionee <lilg><2013-04-17> add for CR00795461 end
								
								Log.d("NoteActivity------mediaFileName: " + mediaFileName);
								startMediaPlaying(mediaFileName, mediaInfoArray[2] + NoteMediaManager.TAG_MIDDLE + mediaInfoArray[3]);
							}
						}
					}
				}
			}
		});
		// Gionee <lilg><2013-03-08> add for play media info when media clicked
		// end
		mNoteDatas = new ArrayList<Note>();
		if (_id == -1 && folderId == -1) {
			note = new Note();
			note.setBgColor("0");
			note.setIsFolder("no");
			note.setParentFile("no");
			note.setWidgetId(String.valueOf(widgetId));
			note.setWidgetType(String.valueOf(widgetType));
			note.setNoteFontSize(String.valueOf(noteFontSize));
			CommonUtils.isAbledAdd(this, createNoteItem, true);
			initNewNote();
		} else if (_id != -1 && folderId == -1) {
			noteTopEdit.setVisibility(View.GONE);
			noteBottom.setVisibility(View.GONE);
			// cao
			responseGesture = true;
			UtilsQueryDatas.queryNotesInFolder(folderId,HomeActivity.mTempNoteList,mNoteDatas);
			Log.d("NoteActivity------bgColor: " + note.getBgColor());
			setTextForTextView(note.getContent().trim());
			// gn lilg 2013-02-27 modify for note content pre deal end
			// gn pengwei 20130227 add for CR00774114 begin
			mCustomView.setEnabled(false);
			CommonUtils.isAbledAdd(this, createNoteItem, false);
			// gn pengwei 20130227 add for CR00774114 end
			disableLabel();
			initViewPager(mNoteDatas,_id);
		} else if (_id == -1 && folderId != -1) {
			note = new Note();
			note.setBgColor("0");
			note.setIsFolder("no");
			note.setNoteFontSize(String.valueOf(noteFontSize));
			note.setWidgetId(String.valueOf(widgetId));
			note.setWidgetType(String.valueOf(widgetType));
			note.setParentFile(String.valueOf(folderId));
			handler.removeMessages(SHOWVIEWPANEL);
			CommonUtils.isAbledAdd(this, createNoteItem, true);
			initNewNote();
		} else if (_id != -1 && folderId != -1) {
			noteTopEdit.setVisibility(View.GONE);
			noteBottom.setVisibility(View.GONE);
//			noteEditText.setVisibility(View.GONE);
			// cao
			responseGesture = true;
			Note note = dbo.queryOneNote(this,_id);
			if(note != null){
				folderId = note.getParentId();
			}
			UtilsQueryDatas.queryNotesIsInFolder(folderId,HomeActivity.mTempNoteList,mNoteDatas);

			setTextForTextView(note.getContent().trim());
			// gn lilg 2013-02-27 modify for note content pre deal end

			CommonUtils.isAbledAdd(this, createNoteItem, false);
			// gn pengwei 20130227 add for CR00774114 begin
			mCustomView.setEnabled(false);
			// gn pengwei 20130227 add for CR00774114 end
			disableLabel();
			initViewPager(mNoteDatas,_id);
		}

		// gn lilg 2013-02-19 add for media record begin
		mLayoutMediaRecorder = (LinearLayout) findViewById(R.id.layout_media_recorder);
		mCurrentRecordTime = (TextView) findViewById(R.id.tv_current_record_time);
		mBtnStopRecord = (ImageButton) findViewById(R.id.btn_stop_record);
		mBtnStopRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NoteActivity------click button stop recording!");
				/*
				 * if(handler != null){
				 * handler.sendEmptyMessage(FLAG_STOP_RECORDER_RECORDING); }
				 */
				stopMediaRecording(save2DB);
			}
		});
		mLayoutMediaPlayer = (LinearLayout) findViewById(R.id.layout_media_player);
		mCurrentPlayTime = (TextView) findViewById(R.id.tv_current_play_time);
		mBtnStopPlay = (ImageButton) findViewById(R.id.btn_stop_play);
		mBtnStopPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("NoteActivity------click button stop playing!");
				if (handler != null) {
					handler.sendEmptyMessage(FLAG_STOP_PLAYER_PLAYING);
				}
			}
		});
		// gn lilg 2013-02-19 add for media record end

		// Gionee <lilg><2013-03-09> add for media player progress bar begin
		mProgressBarMediaPlayer = (ProgressBar) findViewById(R.id.progressBar_media_player);
		// Gionee <lilg><2013-03-09> add for media player progress bar end
		mProgressBarMediaRecord = (ProgressBar) findViewById(R.id.progressBar_media_record);
		initMenuAndAttachment();
	}
	//Gionee <pengwei><20130826> modify for CR00873479 end
	
	private NotePagerAdapter notePageAdpter;
	private List<View> views;
	private void initViewPager(List<Note> mNoteArr,int mCurIdInt) {
	    if(null == mNoteArr){
	        Log.e("NoteActivity-initViewPager null == mNoteArr");
	        return;
	    }
	    notePageAdpter = new NotePagerAdapter(mNoteArr,this);
	    notePageAdpter.setOnDownListener(new IOnDownListener() {

	        @Override
	        public void onDown(Note note) {
	            enterIntoEdit(note);
	        }
	    });
        int posInt = 0;
	    // Gionee <wangpan><2014-10-28> modify for CR01402657 begin
        if(mNoteArr.size() != 0){
            posInt = queryCurPosition(mNoteArr,mCurIdInt);
            note = mNoteArr.get(posInt);
        }
	    // Gionee <wangpan><2014-10-28> modify for CR01402657 end
        browseView.setAdapter(notePageAdpter);
        browseView.setCurrentItem(posInt);
        browseView.setVisibility(View.VISIBLE);
        // Gionee <wangpan><2014-10-31> modify for CR01404637 begin
        String title = note.getTitle() != null ? note.getTitle()
                : getResources().getString(
                        R.string.note_new_title_label);
        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
        //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
        //mCustomView.setText(CommonUtils.getTitleString(title));
          mCustomView.setText(getTitleString(title));
        //Gionee <wangpan><2014-05-21> modify for CR01268739 end
        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
        // Gionee <wangpan><2014-10-31> modify for CR01404637 end
	    browseView.setOnPageChangeListener(new OnPageChangeListener() {

	        @Override
	        public void onPageSelected(int position) {
	            // TODO Auto-generated method stub
	            note = mNoteDatas.get(position);
	            String title = note.getTitle() != null ? note.getTitle()
	                    : getResources().getString(
	                            R.string.note_new_title_label);
                //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	            //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
	            //mCustomView.setText(CommonUtils.getTitleString(title));
                  mCustomView.setText(getTitleString(title));
	            //Gionee <wangpan><2014-05-21> modify for CR01268739 end
                //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
	        }

	        @Override
	        public void onPageScrolled(int arg0, float arg1, int arg2) {
	            // TODO Auto-generated method stub
	            Log.v("NoteActivity---onPageScrolled------------onPageScrolled");
	        }

	        @Override
	        public void onPageScrollStateChanged(int position) {
	            // TODO Auto-generated method stub
	            Log.v("NoteActivity---onPageScrollStateChanged------------onPageScrollStateChanged");

	        }
	    });
	    browseView.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View view) {
	            // TODO Auto-generated method stub
	            Log.v("NoteActivity---browseView.setOnClickListener---");

	        }
	    });
	    Log.d("NoteActivity---initViewPager---2---" + System.currentTimeMillis());
	}
	
	private int queryCurPosition(List<Note> noteArr,int curNoteId){
		int curPosInt = 0;
		if(noteArr == null){
			return curPosInt;
		}
		for(int i = 0;i<noteArr.size();i++){
			if(curNoteId == noteArr.get(i).getNoteId()){
				curPosInt = i;
				break;
			}
		}
		return curPosInt;
	}
	
	private MenuItem deleteImageItem;
	private View mActionBarView;
	private MenuItem shareItem;
	private MenuItem noteChangeBgc;
	private MenuItem mNoteSendToLaunch;
	private static final String DEVICE_MODEL = "E6,E7";
    
	private void showMenuItem() {
		try {
			TextView pageTv = (TextView) views.get(notePage).findViewById(
					R.id.note_show_content_view);
			if (pageTv.getText().length() < 1) {
				// gionee 20121204 jiating modify for CR00739157 begin
				// noteShare.setVisible(false);
				CommonUtils.isAbledAdd(this, createNoteItem, false);
				shareItem.setEnabled(false);
				// gionee 20121204 jiating modify for CR00739157 end
			} else {
				CommonUtils.isAbledAdd(this, createNoteItem, true);
				shareItem.setEnabled(true);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("NoteActivity---showMenuItem---");
		}
	}
	
	private void isResponseGesture(boolean flag){
		if(noteAttchmentItem != null)
		{
			noteAttchmentItem.setEnabled(flag);
		}
		if(noteChangeBgc != null){
			noteChangeBgc.setEnabled(flag);
		}
	}
	

	private void inportOrExport() {
		Intent intent = new Intent();
		intent.setClass(this, ImportExportActivity.class);
		startActivity(intent);
	}

	//Gionee <pengwei><20130702> modify  for CR00832305 begin
	private void showNoteNameDialog() {
		// gn pengwei 20121126 add for statistics begin
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_INPUT_TITLE);
		// gn pengwei 20121126 add for statistics end
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(
						R.layout.dialog_layout_new_folder_white,
						(ViewGroup) findViewById(R.id.dialog_layout_new_folder_root));
		final AmigoEditText editText = (AmigoEditText) layout
				.findViewById(R.id.et_dialog_new_folder);

		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				TITLE_MAX_LENGTH) });

		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s != null && s.length() >= TITLE_MAX_LENGTH) {
					Toast.makeText(NoteActivity.this,
							R.string.note_title_max_length, Toast.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

		if (note.getTitle() == null || "".equals(note.getTitle().trim())) {
			editText.setHint(getResources().getString(
					R.string.note_new_title_label));
		} else {
			editText.setText(note.getTitle());
			editText.setSelection(note.getTitle().length());
		}
		AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(NoteActivity.this,CommonUtils.getTheme());
		builder.setView(layout);
		builder.setTitle(R.string.note_new_title_label)
				.setPositiveButton(R.string.Ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								String title = "";
								// gn pengwei 20121126 modify for CR00746793
								// begin
								if (editText.getText() != null) {
									title = editText.getText().toString()
											.trim().replaceAll("\n", " ");
									if (!"".equals(title)) {
										note.setTitle(title);
                                        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	                                    //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
										//mCustomView
										//		.setText(CommonUtils.getTitleString(title));
                                        mCustomView
												.setText(getTitleString(title));
		                                //Gionee <wangpan><2014-05-21> modify for CR01268739 end
                                        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
									} else {
										note.setTitle(null);
										String titleStr = note.getTitle() != null ? note
												.getTitle()
												: getResources()
														.getString(
																R.string.note_new_title_label);
                                        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	                                    //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
										//mCustomView
										//		.setText(CommonUtils.getTitleString(titleStr));
                                        mCustomView
												.setText(getTitleString(titleStr));
	                                    //Gionee <wangpan><2014-05-21> modify for CR01268739 end
                                        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
									}
									// gn pengwei 20121126 modify for CR00746793
									// end
									note.setUpdateDate(dbo.getDate());
									note.setUpdateTime(dbo.getTime());
									dbo.updateNote(NoteActivity.this, note);
								}
							}
						})
				.setNegativeButton(R.string.delete_note_dialog_cancle,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).setCancelable(true).show();
//tangzz add for inputmethod begin CR01442825 
		CommonUtils.showInputMethod(editText);
//tangzz add for inputmethod end CR01442825 
	}

	private void showDeleteDialog() {
		new AmigoAlertDialog.Builder(NoteActivity.this, CommonUtils.getTheme())
				.setTitle(R.string.delete_note_dialog_title)
				.setMessage(R.string.note_delete_note_dialog_body)
				// gionee lilg 2013-01-16 modify for new demands begin
				// .setIcon(android.R.drawable.ic_dialog_alert)
				// gionee lilg 2013-01-16 modify for new demands end
				.setPositiveButton(R.string.delete_note_dialog_sure,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								// gn pengwei 20130122 modify for CR00766172
								// begin
								if (note.getId() != null) {
									// gn pengwei 20130122 modify for CR00766172
									// end

									// gn lilg 2013-03-04 add for delete media
									// files in the sdcard begin
									closeMediaRecorderOrPlayer(save2DB);

									List<MediaInfo> mediaInfoList = dbo
											.queryMeidas(NoteActivity.this,
													note.getId());
									if (mediaInfoList != null
											&& mediaInfoList.size() > 0) {
										for (MediaInfo mediaInfo : mediaInfoList) {
											if(mediaInfo != null && mediaInfo.getMediaFileName().contains(getResources().getString(R.string.path_note_media))){
												Log.d("NoteActivity------path contains 备份/便签多媒体: " + mediaInfo.getMediaFileName().contains(getResources().getString(R.string.path_note_media)));
//												FileUtils.deleteFile(mediaInfo.getMediaFileName());
												String deleteFile = FileUtils.getPathByPathType(mediaInfo.getMediaFileName().substring(0, 1)) + FileUtils.getSubPathAndFileName(mediaInfo.getMediaFileName());
												Log.d("NoteActivity------delete file: " + deleteFile);
												FileUtils.deleteFile(deleteFile);
											}
										}
									}
									// gn lilg 2013-03-04 add for delete media
									// files in the sdcard end

									dbo.deleteNote(NoteActivity.this, note);

									WidgetUtils
											.updateWidget(NoteActivity.this,
													Integer.parseInt(note
															.getWidgetId()),
													Integer.parseInt(note
															.getWidgetType()));
									UtilsQueryDatas.deleteNote(note, HomeActivity.mTempNoteList);
									// gn pengwei 20130124 modify for CR00767029
									// begin
									note.setId(null);
									// gn pengwei 20130124 modify for CR00767029
									// end
								}
								save2DB = false;
								finish();
							}
						})
				.setNegativeButton(R.string.delete_note_dialog_cancle,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).setCancelable(true).show();
	}
	//Gionee <pengwei><20130702> modify  for CR00832305 end
	@Override
	protected void onPause() {
		Log.i("NoteAcitivy------onPause begin!");
		try {
			
			// Gionee <lilg><2013-04-10> add for note upgrade begin
			((NoteApplication) getApplication()).unregisterVersionCallback(this);
			// Gionee <lilg><2013-04-10> add for note upgrade end
			
			super.onPause();
			if(responseGesture){
				return;
			}
			// gn lilg 2013-02-20 add for stop recording begin
			closeMediaRecorderOrPlayer(save2DB);
			// gn lilg 2013-02-20 add for stop recording begin

			String content = noteEditText.getText() != null ? noteEditText
					.getText().toString().trim() : "";
			// gn pengwei 20121126 modify for CR00746793 begin
			if ((note.getTitle() == null || "".equals(note.getTitle()))
					&& content.length() <= 0) {
				super.onPause();
				// gn pengwei 20120107 add for CR00760779 begin
				if (note.getId() != null) {
					dbo.deleteNote(this, note);
					UtilsQueryDatas.deleteNote(note, HomeActivity.mTempNoteList);
					// gn pengwei 20130124 modify for CR00767029 begin
					note.setId(null);
					// gn pengwei 20130124 modify for CR00767029 end
					Log.d("NoteAcitivy------onPause---ID---null");
				}
				// gn pengwei 20120107 add for CR00760779 end
				WidgetUtils.updateWidget(this,
						Integer.parseInt(note.getWidgetId()),
						Integer.parseInt(note.getWidgetType()));
				return;
			}
			// gn pengwei 20121126 modify for CR00746793 end
			if (!save2DB) {
				return;
			}

			refreshAlarm(note);
			note.setUpdateDate(dbo.getDate());
			note.setUpdateTime(dbo.getTime());
			// Gionee pengwei 2013-3-11 modify for CR00780549 begin
			if (note.getTitle() == null || "".equals(note.getTitle())) {
				if (content.length() > 0) {
					String noteTitleTemp = CommonUtils.noteContentPreDeal(content);
					if (noteTitleTemp == null || "".equals(noteTitleTemp)) {
						note.setTitle(this.getResources().getString(
								R.string.no_title));
					}
				}
			}
			// Gionee pengwei 2013-3-11 modify for CR00780549 end
			// gn pengwei 20130122 for CR00766172 begin
			if (note.getId() != null) {
				note.setContent(content);
				dbo.updateNote(this, note);
				UtilsQueryDatas.changeNote(note, HomeActivity.mTempNoteList);
			} else if (note.getId() == null) {
				note.setContent(content);
				long noteId = dbo.createNote(this, note);
				note.setId(String.valueOf(noteId));
				note.setContent(content);
				dbo.updateNote(this, note);
				UtilsQueryDatas.addNote(note, HomeActivity.mTempNoteList);
			}
			WidgetUtils.updateWidget(this,
					Integer.parseInt(note.getWidgetId()),
					Integer.parseInt(note.getWidgetType()));
			Log.d("NoteActivity------noteFontSize: " + noteFontSize);

			// gn lilg 2013-01-07 modify for location begin
			onDismissDialog();
			// gn lilg 2013-01-07 modify for location end
			// gn pengwei 20130122 for CR00766172 end
			
			// Gionee <lilg><2013-07-01> add for CR00830261 begin
			NoteMediaManager.getInstances(getApplicationContext(), handler).release();
			// Gionee <lilg><2013-07-01> add for CR00830261 end
		} catch (Exception e) {
			Log.e("NoteActivity------onPause exception!", e);
		}
//		saveNoteIDAndFolder();
		Statistics.onPause(this);
		Log.i("NoteAcitivy------onPause end!");
	}

	private void shareNote() {
		// gn pengwei 20121126 add for statistics begin
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_SHARE);
		// gn pengwei 20121126 add for statistics end
		Log.i("GN_note---NoteActivity------share note start!");

		// gn lilg 2013-03-02 modify for CR00774631 begin
		// CommonUtils.shareNote(this, CommonUtils.DEFAULT_MIMETYPE,
		// noteEditText.getText().toString());
		// Gionee <lilg><2013-4-8> modify for CR00794421 begin
		String mContent = "";
		if(responseGesture){
			if(note != null){
				mContent = note.getContent();
			}
		}else{
				mContent = noteEditText.getText()
					.toString();
		}
		// Gionee <lilg><2013-4-8> modify for CR00794421 end
		int result = CommonUtils.shareNote(this, CommonUtils.DEFAULT_MIMETYPE,
				CommonUtils.noteContentPreDeal(mContent));
		if (result == -1) {
			Toast.makeText(this,
					getResources().getString(R.string.share_note_empty_toast),
					Toast.LENGTH_SHORT).show();
		}
		// gn lilg 2013-03-02 modify for CR00774631 end

		Log.d("GN_note---NoteActivity------share note end!");

	}

	@Override
	public boolean onDown(MotionEvent e) {
		System.out.println("onDown");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}
    //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	private String getTitleString(String title) {
		if (title == null) {
			return "";
		}
		if (title.length() <= 11) {
			return title;
		} else {
			return title.substring(0, 11) + "...";
		}
		// TODO Auto-generated method stub
    }
   //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
	public void setLocationResult(String address) {
		hasLocation = true;
		updateAddress(address);
	}

	@Override
	protected void onStop() {
		Log.i("NoteActivity------onStop!");
		// gn lilg 2013-01-18 add for location timeout begin
		onDismissDialog();
		
		// Gionee <lilg><2013-04-02> modify for location with google api begin
//		if (mGNLocateService != null) {
//			mGNLocateService.stop();
//		}
		if (mGNLocateService2 != null) {
			mGNLocateService2.releaseLocation();
		}
		// Gionee <lilg><2013-04-02> modify for location with google api end
		
		if (handler != null) {
			handler.removeCallbacks(locTimeOutRunnable);
		}
		// gn lilg 2013-01-18 add for location timeout end
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("NoteActivity------onDestroy, " + new Date());

		// gn lilg 2013-01-18 add for release noteMediaManager begin
		// Gionee <lilg><2013-07-01> add for CR00830261 begin
		NoteMediaManager.getInstances(getApplicationContext(), handler).release();
		// Gionee <lilg><2013-07-01> add for CR00830261 end
		// gn lilg 2013-01-18 add for release noteMediaManager end

		// gn lilg 2013-01-18 add for location timeout begin
		if (handler != null) {
			handler.removeCallbacks(locTimeOutRunnable);
		}
		// gn lilg 2013-01-18 add for location timeout end

		// GN pengwei 2012-12-19 Commented for CR00751323 begin
		// if(alarmReceiver!=null){
		// unregisterReceiver(alarmReceiver);
		// alarmReceiver=null;
		// }
		// GN pengwei 2012-12-19 Commented out for CR00751323 end
		// Gionee 20121101 jiating begin
		if (InstallWidgetResultReceiver != null) {
			unregisterReceiver(InstallWidgetResultReceiver);
			InstallWidgetResultReceiver = null;
		}
		// Gionee 20121101 jiating end

		// GN pengwei 2012-11-13 add begin
		if (alarmRefreshReceiver != null) {
			unregisterReceiver(alarmRefreshReceiver);
			alarmRefreshReceiver = null;
		}
		// GN pengwei 2012-11-13 add end

		// GN pengwei 2012-11-13 add begin
		if (noteRefreshReceiver != null) {
			unregisterReceiver(noteRefreshReceiver);
			noteRefreshReceiver = null;
		}
		if (mSDReceiver != null) {
			unregisterReceiver(mSDReceiver);
			mSDReceiver = null;
		}
		// GN pengwei 2012-11-13 add end
		CommonUtils.noteID = "";
		// gn pengwei 20121126 add for statistics begin
		allNodesInFolder.clear();
		allNodesInFolder = null;
		
		// Gionee <lilg><2013-03-27> add for NT028 begin
		if(mClipboardManager != null){
			mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
		}
		// Gionee <lilg><2013-03-27> add for NT028 end
	}

	// hxc add for location end

	private void requestLocation() {
		boolean hasGPSBool = hasGPSDevice(this);
		Log.d("NoteActivity---requestLocation---hasGPSBool == " + hasGPSBool);

		Log.i("NoteActivity------requestLocation!");

		if (checkNetworkState()) {
			
			// Gionee <lilg><2013-04-02> modify for location with google api begin
//			if (mGNLocateService == null) {
//				mGNLocateService = GNLocateService.getInstance(getApplicationContext());
//				setLocationListener();
//			}
			if (mGNLocateService2 == null) {
				mGNLocateService2 = GNLocateService2.getInstance(getApplicationContext());
				setLocationListener();
			}
			// Gionee <lilg><2013-04-02> modify for location with google api end
			// Gionee <pengwei><2013-06-14> modify for CR00823616 begin
			if (hasGPSBool) {
				boolean gpsState = checkGPSState();
				Log.d("NoteActivity------gps state: " + gpsState);
				if (gpsState) {
					// Gionee <lilg><2013-04-02> modify for location with google
					// api begin
					// mGNLocateService.enableGpsProvider();
					// Gionee <lilg><2013-04-02> modify for location with google
					// api end
				}
				if (!gpsState && !hasLocation) {
					Toast.makeText(this,
							getString(R.string.gn_location_nogps_tip),
							Toast.LENGTH_SHORT).show();
				}
			}
			// Gionee <pengwei><2013-06-14> modify for CR00823616 end
			// gionee lilg 2013-01-18 add for location timeout begin
			locationStartTime = System.currentTimeMillis();
			if (locTimeOutRunnable == null) {
				locTimeOutRunnable = new LocationTimeOutRunnable();
			}
			handler.post(locTimeOutRunnable);
			// gionee lilg 2013-01-18 add for location timeout end

			onShowProgressDialog();
			
			// Gionee <lilg><2013-04-02> modify for location with google api begin
//			mGNLocateService.requestLocation();
			mGNLocateService2.requestLocation();
			// Gionee <lilg><2013-04-02> modify for location with google api end

		} else {
			Intent intent = new Intent(
					"gn.android.intent.action.SHOW_3GWIFIALERT");
			intent.putExtra("appname", getPackageName());
			sendBroadcast(intent);
			Toast.makeText(this, getString(R.string.gn_location_nonet),
					Toast.LENGTH_SHORT).show();
		}

	}

	public boolean hasGPSDevice(Context context) {
		LocationManager mgr = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (mgr == null)
			return false;
		List<String> providers = mgr.getAllProviders();
		if (providers == null)
			return false;
		return providers.contains(LocationManager.GPS_PROVIDER);
	}
	
	private void dissmissPopupWindow(PopupWindow pWindow) {
		if (pWindow != null && pWindow.isShowing()) {
			pWindow.dismiss();
		}
	}

	public void updateView() {
		// mAlarmButton.setV
		Log.d("NoteActivity------noteActivity, updateView, note.getAlarmTime(): "
				+ note.getAlarmTime());
		// gionee <wangpan> <2013-12-30> modify for CR00992740 CR00993149 begin
		dealWithNoteIdHome();
		// gionee <wangpan> <2013-12-30> modify for CR00992740 CR00993149 end
		if (note.getAlarmTime().equals(Constants.INIT_ALARM_TIME)) {
			hideAlarm();
		} else {
			showAlarm(note);
		}
	}

	private void updateAddress(String address) {
		// pengwei 20130222 modify for CR00772575 begin
		if (address == null || "".equals(address)) {
			if (note.getAddressName() != null
					&& !"".equals(note.getAddressName())) {
				address = addressText.getText().toString();
			}
		}
		// pengwei 20130222 modify for CR00772575 end
		if (address != null && !"".equals(address)) {
			showAdress(address);
		} else {
			hideAddress();
		}
		// pengwei 20130221 modify for CR00772633 begin
		note.setAddressName(address);
		note.setAddressDetail(address);
		// pengwei 20130221 modify for CR00772633 end
		// gn pengwei 20130225 modify for CR00772372 begin
		dbo.updateNote(NoteActivity.this, note);
		// gn pengwei 20130225 modify for CR00772372 end
		Log.d("NoteActivity---updateAddress" + address);
	}

	// GN pengwei 2012-11-12 add for close prompt NoteActivity's view no refresh
	// begin
	/* broadcast锛宺eceive noteActivity's refresh message */
	private BroadcastReceiver noteRefreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String noteID = intent.getStringExtra("noteID");
			int optInt = intent.getIntExtra("opt", -1);
			if (note == null || note.getId() == null
					|| !note.getId().equals(noteID)) {// judge whether or not
														// delete current page
				return;
			}
			// gionee 20121226 pengwei modify for begin
			note.setAlarmTime(Constants.INIT_ALARM_TIME);
			// gionee 20121226 pengwei modify end
			switch (optInt) {
			case CommonUtils.INTENT_DEL:
				viewClose();
				break;
			case CommonUtils.INTENT_LOOK:

				break;
			case CommonUtils.INTENT_CLOSE:

				break;
			default:
				break;
			}
		}
	};

	// GN pengwei 2012-11-12 add for close prompt NoteActivity's view no refresh
	// end

	private void viewClose() {
		finish();
	}

	// GN pengwei 2012-11-12 add
	// begin
	/* broadcast锛宺eceive noteActivity's refresh message */
	private BroadcastReceiver alarmRefreshReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// gionee 20121226 pengwei modify for begin
			ArrayList<String> noteIDList = intent
					.getStringArrayListExtra("noteIDList");
			if (noteIDList == null || noteIDList.size() == 0
					|| noteIDList.indexOf(note.getId()) == -1) {// judge whether
																// or not
				// delete current page
				return;
			}
			note.setAlarmTime(Constants.INIT_ALARM_TIME);
			// gionee 20121226 pengwei modify for end
			hideAlarm();
			if(views != null && notePage < views.size()){
				hideLookAlarm(views.get(notePage));
			}
			refreshWidget();
		}
	};
	private boolean isRun = false;

	private void refreshWidget() {
		WidgetUtils.updateWidget(this, Integer.parseInt(note.getWidgetId()),
				Integer.parseInt(note.getWidgetType()));
	}

	// GN pengwei 2012-11-12 add
	// end


	// gionee 20121220 jiating modify for CR00747076 config begin
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i("NoteActivity....onConfigurationChanged");
	}

	// gionee 20121220 jiating modify for CR00747076 config end

	// gionee 20121226 pengwei add for CR00753726 config begin
	private void refreshAlarm(Note note) {
		if (!whetherAlarmEffect(note)) {
			note.setAlarmTime(Constants.INIT_ALARM_TIME);
			// mBtnAlarm.setImageResource(R.drawable.gn_alarm);
			// mBtnAlarm.postInvalidate();
		}
	}

	private boolean whetherAlarmEffect(Note note) {
		try {
			String timeStr = note.getAlarmTime();
			if (timeStr == null || "".equals(timeStr)) {
				return true;
			}
			if (Constants.INIT_ALARM_TIME.equals(note.getAlarmTime())) {
				return true;
			}
			long timeAlarm = Long.parseLong(timeStr);
			Calendar calendar = Calendar.getInstance();
			long timeL = calendar.getTimeInMillis();
			Log.i("NoteActivity....whetherAlarmEffect---timeAlarm" + timeAlarm);
			Log.i("NoteActivity....whetherAlarmEffect---timeL" + timeL);
			if (timeAlarm <= timeL) {
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("NoteActivity....whetherAlarmEffect---Exception" + e);
			return false;
		}
	}

	// gionee 20121226 pengwei add for CR00753726 config end

	private void changeBg() {
		// gn pengwei 20120118 modify for CR00765638 begin
		// gn pengwei 20121126 add for statistics begin
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_CHANGE_BACKGROUND);
		// gn pengwei 20121126 add for statistics end
		// gn pengwei 20120118 modify for CR00765638 end
	}

	private void sendToLaunch() {
		// gn pengwei 20120118 modify for CR00765638 begin
		// gn pengwei 20121126 add for statistics begin
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_SENT_TO_DESKTOP);
		// gn pengwei 20121126 add for statistics end
		// gn pengwei 20120118 modify for CR00765638 end
	}

	class LocationTimeOutRunnable implements Runnable {
		@Override
		public void run() {
			Log.i("NoteActivity------LocationTimeOutRunnable run!");

			if (handler != null) {
				long diffTime = System.currentTimeMillis() - locationStartTime;
				Log.d("NoteActivity------System.currentTimeMillis() - locationStartTime: "
						+ diffTime);

				if (diffTime > LOCATION_TIME_OUT_MILLISECONDS) {
					// time out
					Log.d("NoteActivity------locate time out!");
					
					// Gionee <lilg><2013-04-02> modify for location with google api begin
//					if (mGNLocateService != null) {
//						mGNLocateService.stop();
//					}
					if (mGNLocateService2 != null) {
						mGNLocateService2.releaseLocation();
					}
					// Gionee <lilg><2013-04-02> modify for location with google api end
					
					onDismissDialog();
					handler.removeCallbacks(this);
					Toast.makeText(
							NoteActivity.this,
							getResources()
									.getString(R.string.gn_location_error),
							Toast.LENGTH_SHORT).show();
				} else {
					handler.postDelayed(this, 1000);
				}
			}
		}
	}

	public void onShowProgressDialog() {
		Log.i("NoteActivity------onShowProgressDialog!");
		onDismissDialog();
		if (mProgressDialog == null) {
			mProgressDialog = new AmigoProgressDialog(this);
			mProgressDialog.setMessage(getString(R.string.gn_location_loading));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}
	}

	public void onDismissDialog() {
		Log.i("NoteActivity------onDismissDialog!");
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private boolean checkNetworkState() {
		Log.i("NoteActivity------checkNetworkState!");
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable() && info.isConnected()) {
			Log.d("NoteActivity------network state: " + true);
			return true;
		} else {
			Log.d("NoteActivity------network state: " + false);
			return false;
		}
	}

	private boolean checkGPSState() {
		Log.d("NoteActivity------checkGPSState!");
		LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager != null) {
			return mLocationManager
					.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		Log.i("NoteActivity------onBackPressed!");
		// gn lilg 2013-01-23 add for location begin
		onDismissDialog();
		
		// Gionee <lilg><2013-04-02> modify for location with google api begin
//		if (mGNLocateService != null) {
//			mGNLocateService.stop();
//		}
		if (mGNLocateService2 != null) {
			mGNLocateService2.releaseLocation();
		}
		// Gionee <lilg><2013-04-02> modify for location with google api begin
		
		if (handler != null) {
			handler.removeCallbacks(locTimeOutRunnable);
		}
		// gn lilg 2013-01-23 add for location end

		// gn lilg 2013-02-20 add for stop recording begin
		closeMediaRecorderOrPlayer(save2DB);
		// gn lilg 2013-02-20 add for stop recording begin

		super.onBackPressed();
	}

	private void changeTextColor(Note note) {
		noteEditText.setTextColor(ResourceParser
				.getNoteGridContentColor(Integer.parseInt(note.getBgColor())));
		timeShow.setTextColor(ResourceParser.getNoteGridContentColor(Integer
				.parseInt(note.getBgColor())));
	}

	
	private void showAlarm(Note note) {
		// mBtnAlarm.setImageResource(R.drawable.gn_alarm_orange);
		if (labelAddress.getVisibility() == View.INVISIBLE) {
			labelAddress.setVisibility(View.GONE);
		}
		String alarmTime = com.gionee.note.utils.DateUtils.format(
				new Date(Long.valueOf(note.getAlarmTime())),
				"yyyy-MM-dd HH:mm:ss");
		alarmText.setText(alarmTime);
		labelAlarm.setVisibility(View.VISIBLE);
	}


	
	private void hideAlarm() {
		// mBtnAlarm.setImageResource(R.drawable.gn_alarm);
		if (labelAddress.getVisibility() == View.GONE) {
			labelAlarm.setVisibility(View.INVISIBLE);
		} else {
			labelAlarm.setVisibility(View.GONE);
		}
	}


	
	private void hideLookAlarm(View view) {
		// mBtnAlarm.setImageResource(R.drawable.gn_alarm);
		RelativeLayout labelAlarmView = (RelativeLayout) view.findViewById(R.id.label_alarm_view);
		RelativeLayout labelAddressView = (RelativeLayout) view.findViewById(R.id.label_address_view);
		if (labelAddressView.getVisibility() == View.GONE) {
			labelAlarmView.setVisibility(View.INVISIBLE);
		} else {
			labelAlarmView.setVisibility(View.GONE);
		}
		if(notePageAdpter != null){
			notePageAdpter.notifyDataSetChanged();
		}
	}
	
	
	private void deleteAlarm(Note note) {
		alarmText.setText("");
		note.setAlarmTime(Constants.INIT_ALARM_TIME);
		dbo.updateNote(NoteActivity.this, note);
		AlarmUtils.cancelAlarm(NoteActivity.this, note);
		hideAlarm();
		// dissmissPopupWindow(mAlarmPopupWindow);
	}

	private void setAlarmTime() {
		dissmissPopupWindow(mAttachmentPopupWindow);
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_ALARM);
		// gn pengwei 20121126 add for statistics end
		String alarmTime = note.getAlarmTime();
		if (Constants.INIT_ALARM_TIME.equals(alarmTime)) {
			AlarmUtils.setAlarm(NoteActivity.this, note);
			return;
		}
		AlarmUtils.setAlarm(NoteActivity.this, note);
	}

	private void setAddress() {
		dissmissPopupWindow(mAttachmentPopupWindow);
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_POSITIONING);
		// gn pengwei 20121126 add for statistics end
		if (!hasLocation) {
			// gn lilg 2013-01-07 modify for location begin
			requestLocation();
			return;
		}
		// gn lilg 2013-01-07 modify for location begin
		requestLocation();
		// gn lilg 2013-01-07 modify for location end
		hasLocation = true;
		updateAddress("");
	}

	private void deleteAddress() {
		// pengwe 20120221 modify for CR00772646 begin
		hasLocation = false;
		addressText.setText("");
		note.setAddressName("");
		note.setAddressDetail("");
		dbo.updateNote(NoteActivity.this, note);
		updateAddress("");
		// pengwe 20120221 modify for CR00772646 end
	}

	private void hideAddress() {
		if (labelAlarm.getVisibility() == View.GONE) {
			labelAddress.setVisibility(View.INVISIBLE);
		} else if (labelAlarm.getVisibility() == View.VISIBLE) {
			labelAddress.setVisibility(View.GONE);
		}
		labelDivision.setVisibility(View.GONE);
	}
	
	private void showAdress(String address) {
		if (labelAlarm.getVisibility() == View.INVISIBLE) {
			labelAlarm.setVisibility(View.GONE);
		}
		// Gionee <pengwei><2013-3-18> modify for CR00785551 begin
		int paddingLeft = CommonUtils.dip2px(this,
				noteEditText.getPaddingLeft());
		int paddingRight = CommonUtils.dip2px(this,
				noteEditText.getPaddingRight());
		int marginLeft = CommonUtils.dip2px(this,8);
		int marginRight = CommonUtils.dip2px(this,72);
		int picWid = com.gionee.note.content.Session.getScreenWight() - paddingLeft
				- paddingRight - marginLeft - marginRight;
		addressText.setMaxWidth(picWid * 4 / 5);
		// Gionee <pengwei><2013-3-18> modify for CR00785551 end
		labelDivision.setVisibility(View.VISIBLE);
		labelAddress.setVisibility(View.VISIBLE);
		addressText.setText(address);
	}


	
	private void dealWithNoteId() {
		if (_id != -1) {
			note = dbo.queryOneNote(NoteActivity.this, _id);
			// modify by caojb for CR00725202
			// crash when click widget after clear data
			// gn pengwei 20130122 modify for CR00766723 begin
			// gn pengwei 20130124 modify for CR00767758 begin
			if (note.getId() == null) {
				this._id = -1;
			} else
			// gn pengwei 20130122 modify for CR00766723 end
			// gn pengwei 20130124 modify for CR00767758 end
			{
				folderId = "no".equals(note.getParentFile()) ? -1 : Integer
						.parseInt(note.getParentFile());
			}
		}
	}

	// gn pengwei 20130225 add for CR00772372 begin
	private void dealWithNoteIdHome() {
		if (note.getId() != null) {
			Log.i("GN_note---NoteActivity------dealWithNoteIdHome---"
					+ note.getId());
			note = dbo.queryOneNote(NoteActivity.this,
					Integer.valueOf(note.getId()));
		}
	}

	// gn pengwei 20130225 add for CR00772372 end

	private void clickAttachment() {
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_CLICK_ATTACHMENT);

		View parent = getLayoutInflater().inflate(R.layout.note_attachment,
				null);
		if (mAttachmentPopupWindow == null) {
	      //GIONEE wanghaiyan 20150317 modify for CR01454362 begin
			mAttachmentPopupWindow = new PopupWindow(parent,
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
              ColorDrawable dw = new ColorDrawable(-00000); 
			mAttachmentPopupWindow.setBackgroundDrawable(dw);
            mAttachmentPopupWindow.update();
        //   mAttachmentPopupWindow = new PopupWindow(parent,
			//		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
			//mAttachmentPopupWindow.setBackgroundDrawable(new BitmapDrawable());
	   //GIONEE wanghaiyan 20150317 modify for CR01454362 end
			mAttachmentPopupWindow.setAnimationStyle(R.style.AnimationFade);
  
	//private LinearLayout mBtNoteVoice;
	//private LinearLayout mBtNoteLocation;
	//private LinearLayout mBtNoteAlarm;
            mBtNoteVoice=(LinearLayout)findViewById(R.id.bt_note_voice);
            //mBtNoteVoice.setClickable(true);

            mBtNoteLocation=(LinearLayout)findViewById(R.id.bt_note_location);
            //mBtNoteLocation.setClickable(true);

            mBtNoteAlarm=(LinearLayout)findViewById(R.id.bt_note_alarm);
            //mBtNoteAlarm.setClickable(true);

			noteVoice = (ImageView) parent.findViewById(R.id.note_voice);
			mBtnAddress = (ImageView) parent.findViewById(R.id.note_location);
			mBtnAlarm = (ImageView) parent.findViewById(R.id.note_alarm);
		}
		dissmissPopupWindow(mAttachmentPopupWindow);
		// Gionee <wangpan><2014-05-15> modify for CR01249465 begin
		int yoff = CommonUtils.dip2px(getApplicationContext(), -5);
		mAttachmentPopupWindow.showAsDropDown(mBtnAttachment, 0, yoff);
		// Gionee <wangpan><2014-05-15> modify for CR01249465 end
		if ((note.getAddressName() != null && !"".equals(note.getAddressName()))) {
			mBtnAddress.setImageResource(R.drawable.gn_location_orange);
			Log.i("GN_note---NoteActivity------note.getAddressName()---"
					+ note.getAddressName());
		} else {
			mBtnAddress.setImageResource(R.drawable.gn_location);
			Log.i("GN_note---NoteActivity------dealWithNoteIdHome---note.getAddressName()---null");
		}
		if (note.getAlarmTime() == null
				|| Constants.INIT_ALARM_TIME.equals(note.getAlarmTime())) {
			Log.i("GN_note---NoteActivity------dealWithNoteIdHome---note.getAlarmTime()---null");
			mBtnAlarm.setImageResource(R.drawable.gn_alarm);
		} else {
			Log.i("GN_note---NoteActivity------note.getAlarmTime()---"
					+ note.getAlarmTime());
			mBtnAlarm.setImageResource(R.drawable.gn_alarm_orange);
		}

		List<MediaInfo> mediaLists = dbo.queryMeidas(this, note.getId());
		if (mediaLists == null || mediaLists.size() == 0) {
			noteVoice.setImageResource(R.drawable.gn_voice);
		} else {
			noteVoice.setImageResource(R.drawable.gn_voice_orange);
		}

		noteVoice.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				CommonUtils.showToast(NoteActivity.this, getResources()
						.getString(R.string.note_recording));
				return false;
			}
		});
		noteVoice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dissmissPopupWindow(mAttachmentPopupWindow);

				attachmentDisenabled();
				handler.sendEmptyMessage(FLAG_START_MEDIA_RECORDER);

			}
		});
		mBtnAddress.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				CommonUtils.showToast(NoteActivity.this, getResources()
						.getString(R.string.location));
				return false;
			}
		});
		mBtnAddress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setAddress();
				dissmissPopupWindow(mAttachmentPopupWindow);
			}
		});
		mBtnAlarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// gn pengwei 20121126 add for statistics begin
				setAlarmTime();
				dissmissPopupWindow(mAttachmentPopupWindow);
			}
		});
		mBtnAlarm.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				CommonUtils.showToast(NoteActivity.this, getResources()
						.getString(R.string.alarm));
				return false;
			}
		});
	}

	private void attachmentEnabled() {
		 noteAttchmentItem.setEnabled(true);
	}

	private void attachmentDisenabled() {
		 noteAttchmentItem.setEnabled(false);
	}

	private void disableLabel() {
		alarmContext.setEnabled(false);
		alarmBtn.setEnabled(false);
		addressContext.setEnabled(false);
		addressBtn.setEnabled(false);
	}

	private void enableLabel() {
		alarmContext.setEnabled(true);
		alarmBtn.setEnabled(true);
		addressContext.setEnabled(true);
		addressBtn.setEnabled(true);
	}

	private void startMediaRecording() {
		Log.i("NoteAcitivy------start recording begin!");
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_ADD_RECORD);
		if (mMediaManager == null) {
			mMediaManager = NoteMediaManager.getInstances(
					getApplicationContext(), handler);
		}
		if (mMediaManager.isRecording()) {
			Log.e("NoteAcitivy------media is recording!");
			attachmentEnabled();
			return;
		}
		if (mMediaManager.isPlaying()) {
			Log.e("NoteAcitivy------media is playing!");
			attachmentEnabled();
			return;
		}

		// Gionee <lilg><2013-03-20> add for media record number limit begin
		// check the num of this note, if num > 5, will not add a media record.
		int num = -1;
		if(note != null && note.getId() != null){
			num = getMediaRecordNum(this, note.getId());
			Log.d("NoteAcitivy------the media record num of this note is: " + num);
			if(num >= 5){
				// alert user
				if (handler != null) {
					Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
					message.obj = getResources().getString(R.string.message_media_record_num_max);
					message.sendToTarget();
				}
				attachmentEnabled();
				return;
			}
		}
		// Gionee <lilg><2013-03-20> add for media record number limit end

		int flag = FileUtils.FLAG_OTHER;
		int sdcardState = FileUtils.checkSDCardState();
		if(sdcardState == FileUtils.SUCCESS_SDCARD_STATE){
			// will store media record into sdcard
			flag = FileUtils.FLAG_SDCARD;
		}else{
			int internalMemoryState = FileUtils.checkInternalMemoryState();
			if(internalMemoryState == FileUtils.SUCCESS_INTERNAL_MEMORY_STATE){
				// will store media record into internal memory
				flag = FileUtils.FLAG_INTERNAL_MEMORY;
			}else if(internalMemoryState == FileUtils.ERROR_INTERNAL_MEMORY_NOT_EXISTS_OR_UNAVAILABLE){
				if (handler != null) {
					Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
					message.obj = getResources().getString(R.string.message_sdcard_internal_memory_both_not_exists_or_unavailable);
					message.sendToTarget();
				}
				attachmentEnabled();
			}else if(internalMemoryState == FileUtils.ERROR_INTERNAL_MEMORY_MIN_AVAILABLE_STORE){
				if (handler != null) {
					Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
					message.obj = getResources().getString(R.string.message_sdcard_internal_memory_both_storage_is_full);
					message.sendToTarget();
				}
				attachmentEnabled();
			}
		}
		Log.d("NoteAcitivy------the state of sdcard or internal memory: " + flag);
		
		String noteMediaFolderName = note.getMediaFolderName();
		int state = NoteMediaManager.ERROR_RECORDER_START;
		
		if(flag == FileUtils.FLAG_OTHER){
			// sdcard and internal memory both are not ok
			/*if (handler != null) {
				Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
				message.obj = getResources().getString(R.string.message_sdcard_internal_memory_both_not_ok);
				message.sendToTarget();
			}*/
			return;
		}else if(flag == FileUtils.FLAG_SDCARD){
			// adcard is ok
			if(!TextUtils.isEmpty(noteMediaFolderName) && !"-1".equals(noteMediaFolderName)){
				// this note is already has media record
				if(PlatForm.isMTK() && !NoteMediaManager.PATH_TYPE_SD_CARD.equals(noteMediaFolderName.substring(0, 1))){
					// the media record is not store in the sdcard
					if (handler != null) {
						Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
						message.obj = getResources().getString(R.string.message_media_record_add_fail);
						message.sendToTarget();
					}
					attachmentEnabled();
					return;
				}
			}
			Log.d("NoteAcitivy------start recording begin2!");
			//Gionee <wangpan> <2014-03-22> modify for CR01131012 begin
//			String SDPath = getSDCardFilePath();
			String SDPath = FileUtils.getSdcardRealPath();
			//Gionee <wangpan> <2014-03-22> modify for CR01131012 end
			state = mMediaManager.startRecording(note.getMediaFolderName(), SDPath, NoteMediaManager.PATH_TYPE_SD_CARD);
			
		}else if(flag == FileUtils.FLAG_INTERNAL_MEMORY){
			// internal memory is ok
			if(!TextUtils.isEmpty(noteMediaFolderName) && !"-1".equals(noteMediaFolderName)){
				// this note is already has media record
				Log.d("NoteAcitivy------the path of internal memory: " + FileUtils.getInternalMemoryPath().getPath());
				//Gionee <pengwei><2013-11-01> modify for CR00941779 begin
				if(PlatForm.isMTK() && !NoteMediaManager.PATH_TYPE_INTERNAL_MEMORY.equals(noteMediaFolderName.substring(0, 1))){
				//Gionee <pengwei><2013-11-01> modify for CR00941779 end
					// the media record is not store in the internal memory
					if (handler != null) {
						Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
						message.obj = getResources().getString(R.string.message_media_record_add_fail);
						message.sendToTarget();
					}
					attachmentEnabled();
					return;
				}
			}
			
			state = mMediaManager.startRecording(note.getMediaFolderName(), FileUtils.getInternalMemoryPath().getPath() + "/", NoteMediaManager.PATH_TYPE_INTERNAL_MEMORY);
			
		}else{
			// error
			attachmentEnabled();
			return;
		}
		
		// start recording
		// Gionee <lilg><2013-03-12> add for add a dir for the local media file begin
//		int state = mMediaManager.startRecording(note.getMediaFolderName());
		// Gionee <lilg><2013-03-12> add for add a dir for the local media file end

		if (state != NoteMediaManager.SUCCESS_RECORDER_START) {
			if (handler != null) {
				Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
				message.obj = getResources().getString(
						R.string.message_media_recorder_start_error);
				message.sendToTarget();
			}
			attachmentEnabled();
			return;
		}

		// update view
		mLayoutMediaRecorder.setVisibility(View.VISIBLE);
		mLayoutMediaPlayer.setVisibility(View.GONE);
		noteTabRel.setVisibility(View.GONE);
		// timer
		startProgress();
		timeMediaRecording();

		Log.d("NoteAcitivy------start recording end!");
	}

	
	private void timeMediaRecording() {
		Log.d("NoteAcitivy------timing media recording!");

		if (handler != null) {
			Message msg = handler.obtainMessage(FLAG_UPDATE_MEDIA_CURRENT_TIME);
			msg.obj = getDisplayTime(mMediaMinute, mMediaSecond);
			msg.arg1 = TYPE_MEDIA_RECORDER;
			msg.sendToTarget();

			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {

					mMediaSecond++;
					if (mMediaSecond >= 60) {
						mMediaSecond = 0;
						mMediaMinute++;
					}

					Message msg = handler.obtainMessage(FLAG_UPDATE_MEDIA_CURRENT_TIME);
					msg.obj = getDisplayTime(mMediaMinute, mMediaSecond);
					msg.arg1 = TYPE_MEDIA_RECORDER;
					msg.sendToTarget();
					
					// max time of record
					if (mMediaMinute >= NoteMediaManager.RECORD_TIME_MAX_MINUTE && mMediaSecond >= NoteMediaManager.RECORD_TIME_MAX_SECOND) {
						if(handler != null){
							// stop recording
							handler.sendEmptyMessage(FLAG_STOP_RECORDER_RECORDING);
							
							// alert user
							Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
							message.obj = getResources().getString(R.string.message_media_record_time_max);
							message.sendToTarget();
						}
						this.cancel();
					}
				}
			};
			mMediaTimer = new Timer();
			mMediaTimer.schedule(timerTask, 1000, 1000);
		}
	}

	private String getDisplayTime(int mMediaMinute, int mMediaSecond) {
		return (mMediaMinute < 10 ? "0" + mMediaMinute : mMediaMinute) + ":"
				+ (mMediaSecond < 10 ? "0" + mMediaSecond : mMediaSecond);
	}

	private void stopMediaRecording(boolean save2DB) {
		Log.i("NoteAcitivy------stop recording begin!");
		Log.d("NoteAcitivy------save2DB: " + save2DB);

		// update view
		mLayoutMediaRecorder.setVisibility(View.GONE);
		mLayoutMediaPlayer.setVisibility(View.GONE);
		noteTabRel.setVisibility(View.VISIBLE);

		// stop timer
		if (mMediaTimer != null) {
			mMediaTimer.cancel();
		}

		// stop recording
		if (mMediaManager != null) {

			String mediaFileName = mMediaManager.getmRecordFilePath() + "/" + mMediaManager.getmRecordFileName();
			stopProgress();
			mMediaManager.stopRecording();

			if (save2DB) {
				// save media file name to db
				long mediaItemId = saveMediaToDB(mMediaManager.getmRecordPathType() + mMediaManager.getmRecordFilePath());
				// update ui
				if (mediaItemId != -1) {

					Bitmap mediaRecordBitmap = initMediaRecordBitmap(mMediaMinute, mMediaSecond, null);

					// Gionee <lilg><2013-04-07> add for CR00787299 begin
					if(noteEditText != null && noteEditText.getText().toString().length() > 0){
						Log.d("NoteActivity------noteEditText text length: " + noteEditText.getText().toString().length());
						noteEditText.append("\n");
					}
					// Gionee <lilg><2013-04-07> add for CR00787299 end
					
					insertMediaRecordIntoEditText(mMediaManager.getmRecordFileName(), mediaRecordBitmap, mMediaMinute, mMediaSecond);

					// insertMediaButtonIntoEditText();
				}

			} else {
				// delete media file in the sdcard when save2DB is false
				Log.d("NoteActivity------path contains 便签/便签多媒体: " + mediaFileName.contains(getResources().getString(R.string.path_note_media)));
				if(mediaFileName.contains(getResources().getString(R.string.path_note_media))){
					FileUtils.deleteFile(mediaFileName);
				}
			}
		}

		// init
		mMediaMinute = 0;
		mMediaSecond = 0;
		attachmentEnabled();
		Log.d("NoteAcitivy------stop recording end!");
	}

	//Gionee <pengwei><20130626> modify for CR00829888 begin
	private long saveMediaToDB(String mediaFolderName) {

		if (note != null) {
			if (dbo == null) {
				dbo = DBOperations.getInstances(NoteActivity.this);
			}
			String mediaType = NoteMediaManager.TYPE_MEDIA_RECORD;
			String recordNameTag = NoteMediaManager.TAG_PREFIX + mediaType + NoteMediaManager.TAG_MIDDLE + mMediaManager.getmRecordFileName() + NoteMediaManager.TAG_MIDDLE + getDisplayTime(mMediaMinute, mMediaSecond) + NoteMediaManager.TAG_STORE_SUFFIX;
			note.setContent(recordNameTag);
			if (note.getId() == null) {
				// note is not save to db
				long noteId = dbo.createNote(NoteActivity.this, note);
				note.setId(String.valueOf(noteId));
				UtilsQueryDatas.addNote(note, HomeActivity.mTempNoteList);//add by pengwei
			}

			// Gionee <lilg><2013-03-13> add for add a dir for the local media file begin
			if(TextUtils.isEmpty(note.getMediaFolderName()) || "-1".equals(note.getMediaFolderName()) ){
				note.setMediaFolderName(mediaFolderName);

				ContentValues cv = new ContentValues();
				cv.put(DBOpenHelper.MEDIA_FOLDER_NAME, mediaFolderName);
				String whereClause = " _id = ? ";
				String[] whereArgs = new String[]{ note.getId() };
				dbo.updateNote(this, cv, whereClause, whereArgs);
			}
			// Gionee <lilg><2013-03-13> add for add a dir for the local media file end
			
			MediaInfo mediaInfo = new MediaInfo();
			mediaInfo.setMediaType(NoteMediaManager.TYPE_MEDIA_RECORD);
			mediaInfo.setMediaFileName(mMediaManager.getmRecordPathType() + mMediaManager.getmRecordFilePath() + "/" + mMediaManager.getmRecordFileName());
			long mediaItemId = dbo.insertMedia(NoteActivity.this, note,
					mediaInfo);
			return mediaItemId;
		}

		return -1;
	}
	//Gionee <pengwei><20130626> modify for CR00829888 end
	
	private void insertMediaRecordIntoEditText(String recordFileName, Bitmap mediaRecordBitmap, int mMediaMinute, int mMediaSecond){

		String mediaType = NoteMediaManager.TYPE_MEDIA_RECORD;
		String recordNameTag = NoteMediaManager.TAG_PREFIX + mediaType + NoteMediaManager.TAG_MIDDLE + recordFileName + NoteMediaManager.TAG_MIDDLE + getDisplayTime(mMediaMinute, mMediaSecond) + NoteMediaManager.TAG_STORE_SUFFIX;
		noteEditText.insertMediaRecord(this, recordNameTag, mediaRecordBitmap);

		Log.d("NoteAcitivy------recordNameTag: " + recordNameTag);
	}
	
	private void insertMediaRecordIntoTextView(String recordFileName, Bitmap mediaRecordBitmap, int mMediaMinute, int mMediaSecond){

		// create current media into edit text
		String mediaType = NoteMediaManager.TYPE_MEDIA_RECORD;
		String recordNameTag = NoteMediaManager.TAG_PREFIX + mediaType + NoteMediaManager.TAG_MIDDLE + recordFileName + NoteMediaManager.TAG_MIDDLE + getDisplayTime(mMediaMinute, mMediaSecond) + NoteMediaManager.TAG_STORE_SUFFIX;

		final SpannableString spannableString = new SpannableString(recordNameTag);
		ImageSpan imageSpan = new ImageSpan(this, mediaRecordBitmap);
		spannableString.setSpan(imageSpan, 0, recordNameTag.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

		showNoteContent.append(spannableString);

		Log.d("NoteAcitivy------recordNameTag: " + recordNameTag);
	}

	private Bitmap initMediaRecordBitmap(int mMediaMinute, int mMediaSecond, String recordFileName) {

		if (recordFileName == null) {
			recordFileName = mMediaManager.getmRecordFileName().substring(0, mMediaManager.getmRecordFileName().indexOf("."));
		}

		// record info view
		View voiceView = LayoutInflater.from(this).inflate(
				R.layout.note_voice_item, null);
		RelativeLayout linearLayout = (RelativeLayout) voiceView
				.findViewById(R.id.note_voice_label);
		
		// Gionee <lilg><2013-05-11> modify for CR00802473 begin
//		int picWid = com.gionee.note.content.Session.screenWight - paddingLeft - paddingRight;
		// Gionee <wangpan><2014-05-15> modify for CR01249465 begin
		int picWid = com.gionee.note.content.Session.getScreenWight() - noteEditText.getPaddingLeft()*2 - noteEditText.getPaddingRight() * 2;
		// Gionee <wangpan><2014-05-15> modify for CR01249465 end
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(picWid, LayoutParams.MATCH_PARENT));
		// Gionee <lilg><2013-05-11> modify for CR00802473 end

		TextView textViewTimeInfo = (TextView) linearLayout.findViewById(R.id.voice_text);
		textViewTimeInfo.setText(getDisplayTime(mMediaMinute, mMediaSecond));
		textViewTimeInfo.setTextColor(getResources().getColor(R.color.note_media_recorder_text_corlor));
		TextView textViewNameInfo = (TextView) linearLayout.findViewById(R.id.voice_name);
		textViewNameInfo.setText(recordFileName);
		// view to bitmap
		linearLayout.setDrawingCacheEnabled(true);
		linearLayout.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		linearLayout.layout(0, 0, picWid, linearLayout.getMeasuredHeight());

		linearLayout.buildDrawingCache();
		Bitmap cacheBitmap = linearLayout.getDrawingCache();

		if (cacheBitmap != null) {
			Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
			linearLayout.setDrawingCacheEnabled(false);
			if (bitmap != null) {
				return bitmap;
			} else {
				Log.e("NoteAcitivy------bitmap == null!");
			}
		} else {
			Log.e("NoteAcitivy------cacheBitmap == null!");
		}

		return null;
	}

	private void insertMediaButtonIntoEditText() {

		// String btnNameTag = "<gionee_media:8/>";
		String btnNameTag = NoteMediaManager.TAG_PREFIX
				+ Constants.TYPE_MEDIA_BUTTON
				+ NoteMediaManager.TAG_STORE_SUFFIX;
		noteEditText.insertMediaButton(btnNameTag,
				R.drawable.note_media_btn_del, handler);
		// noteEditText.insertMediaButton(this, btnNameTag,
		// R.layout.media_btn_delete_layout, handler);

	}
	
	private void startMediaPlaying(String mediaFile, String mediaDuration){
		Statistics.onEvent(NoteActivity.this, Statistics.NOTE_PLAY_RECORD);
		Log.i("NoteAcitivy------start playing begin!");
		Log.d("NoteAcitivy------media file to be playing: " + mediaFile);

		if (TextUtils.isEmpty(mediaFile)) {
			Log.e("NoteAcitivy------mediaFile to playing is error!");
			return;
		}

		if (TextUtils.isEmpty(mediaDuration)) {
			Log.e("NoteAcitivy------mediaDuration is empty!");
			return;
		}

		String[] timeInfo = mediaDuration.split(":");
		int minute = 0;
		int second = 0;
		if (timeInfo != null && timeInfo.length >= 2) {
			try {
				minute = Integer.parseInt(timeInfo[0]);
				second = Integer.parseInt(timeInfo[1]);
				Log.d("NoteAcitivy------mediaPlayer duration minute: " + minute
						+ ", second: " + second);
			} catch (Exception e) {
				Log.e("NoteAcitivy------exception!", e);
				return;
			}
		}

		if (mMediaManager == null) {
			mMediaManager = NoteMediaManager.getInstances(
					getApplicationContext(), handler);
		}
		if (mMediaManager.isRecording()) {
			Log.e("NoteAcitivy------media is recording!");
			// Gionee <lilg><2013-04-04> add for alert user begin
			if(handler != null){
				Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
				message.obj = getResources().getString(R.string.message_media_recorder_is_recording);
				message.sendToTarget();
			}
			// Gionee <lilg><2013-04-04> add for alert user end
			return;
		}
		if (mMediaManager.isPlaying()) {
			Log.e("NoteAcitivy------media is playing!");
			return;
		}

		// Gionee <lilg><2013-04-04> add for alert user begin
		File mediaFileToPlay = new File(mediaFile);
		Log.e("NoteAcitivy------" + mediaFileToPlay.getName());
		
		if(!mediaFileToPlay.exists()){
			if(handler != null){
				Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
				message.obj = getResources().getString(R.string.message_media_player_file_not_exists);
				message.sendToTarget();
			}
			return;
		}
		// Gionee <lilg><2013-04-04> add for alert user end
		
		// start playing
		int state = mMediaManager.startPlaying(mediaFile);
		if (state != NoteMediaManager.SUCCESS_PLAYER_START) {
			if (handler != null) {
				Message message = handler.obtainMessage(FLAG_ALERT_MESSAGE);
				message.obj = getResources().getString(
						R.string.message_media_player_start_error);
				message.sendToTarget();
			}
			return;
		}

		// update view
		mLayoutMediaRecorder.setVisibility(View.GONE);
		mLayoutMediaPlayer.setVisibility(View.VISIBLE);
		noteTabRel.setVisibility(View.GONE);

		// Gionee <lilg><2013-03-11> add for update the player current progress
		// begin
		Log.d("NoteAcitivy------mediaPlayer progressBar Max is: "
				+ (minute * 60 + second));
		mProgressBarMediaPlayer.setMax(minute * 60 + second);
		mProgressBarMediaPlayer.setProgress(0);
		// Gionee <lilg><2013-03-11> add for update the player current progress
		// end

		timerMediaPlaying(mediaDuration);

		Log.d("NoteAcitivy------start playing end!");
	}

	private void timerMediaPlaying(final String mediaDuration) {
		Log.i("NoteAcitivy------timing media playing!");
		Log.d("NoteAcitivy------mediaDuration: " + mediaDuration);

		if (handler != null) {
			// gn lilg 2013-03-04 modify for CR00775132 begin
			// final int mediaDuration = mMediaManager.getMediaPlayerDuration();
			// gn lilg 2-13-03-04 modify for CR00775132 end

			TimerTask timerTask = new TimerTask() {

				// Gionee <lilg><2013-03-09> add for media progress bar begin
				int currProgress = 0;

				// Gionee <lilg><2013-03-09> add for media progress bar end

				@Override
				public void run() {

					Message msg = handler
							.obtainMessage(FLAG_UPDATE_MEDIA_CURRENT_TIME);
					String currentDisplayTime = getCurrentDisplayTime(mediaDuration);
					msg.obj = currentDisplayTime;
					msg.arg1 = TYPE_MEDIA_PLAYER;
					// Gionee <lilg><2013-03-09> add for media progress bar
					// begin
					msg.arg2 = currProgress++;
					// Gionee <lilg><2013-03-09> add for media progress bar end
					msg.sendToTarget();
				}
			};
			mMediaTimer = new Timer();
			mMediaTimer.schedule(timerTask, new Date(), 1000);
		}
	}

	private String getCurrentDisplayTime(String mediaDuration) {

		int curMinute = mMediaManager.getMediaPlayerCurrentPosition() / 1000 / 60;
		int curSecond = mMediaManager.getMediaPlayerCurrentPosition() / 1000 % 60;

		// gn lilg 2013-03-04 modify for CR00775132 begin
		// int allMinute = mediaDuration/1000/60;
		// int allSecond = mediaDuration/1000%60;

		// return (curMinute < 10 ? "0" + curMinute : curMinute) + ":" +
		// (curSecond < 10 ? "0" + curSecond : curSecond) + "/" + (allMinute <
		// 10 ? "0" + allMinute : allMinute) + ":" + (allSecond < 10 ? "0" +
		// allSecond : allSecond);
		// gn lilg 2013-03-04 modify for CR00775132 end

		return (curMinute < 10 ? "0" + curMinute : curMinute) + ":"
				+ (curSecond < 10 ? "0" + curSecond : curSecond) + "/"
				+ mediaDuration;
	}

	private void stopMediaPlaying() {
		Log.i("NoteAcitivy------stop playing begin!");

		// update view
		mLayoutMediaRecorder.setVisibility(View.GONE);
		mLayoutMediaPlayer.setVisibility(View.GONE);
		noteTabRel.setVisibility(View.VISIBLE);

		// stop timer
		if (mMediaTimer != null) {
			mMediaTimer.cancel();
		}

		// stop playing
		if (mMediaManager != null) {
			mMediaManager.stopPlaying();
		}

		Log.d("NoteAcitivy------stop playing end!");
	}

	private void closeMediaRecorderOrPlayer(boolean save2DB) {
		if (mMediaManager != null) {
			if (mMediaManager.isRecording()) {
				stopMediaRecording(save2DB);
			} else if (mMediaManager.isPlaying()) {
				stopMediaPlaying();
			}
		}
	}

	private void setTextForNoteEditText(String text){
		Log.d("NoteActivity------setTextForNoteEditText, text: " + text);
		if(null == noteEditText){
		    return;
		}
		if(!text.contains(NoteMediaManager.TAG_PREFIX)){
		    // not contains media info
		    Log.d("not contains media info");
		    noteEditText.setText(text);
		}else{
		    // contains media info
		    Log.i("contains media info");
		    // clear noteEditText
		    noteEditText.setText("");

		    // Gionee <lilg><2013-04-08> add for CR00794516 begin
//		    text = text.replaceAll("\n", "");
		    // Gionee <lilg><2013-04-08> add for CR00794516 end

		    int lastPosEnd = 0;
		    //position of  "<gionee_media:"
		    int posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX, lastPosEnd);
		    Log.d("NoteActivity------posBegin: " + posBegin);
		    while(posBegin != -1){
		        // position of "/>"
		        int posEnd = text.indexOf(NoteMediaManager.TAG_PARSE_SUFFIX, posBegin);
		        Log.d("NoteActivity------posEnd: " + posEnd);
		        if(posEnd != -1){
		            // <gionee_media:0:20140312145653.mp3:00:02/>>
		            String[] mediaInfoArray = text.substring(posBegin + NoteMediaManager.TAG_PREFIX.length(), posEnd).split(NoteMediaManager.TAG_MIDDLE);
		            if(mediaInfoArray != null){
		                Log.d("NoteActivity------mediaInfoType: " + mediaInfoArray[0] + ", mediaInfoId: " + mediaInfoArray[1] + ", mediaInfoMinute: " + mediaInfoArray[2] + ", mediaInfoSecond: " + mediaInfoArray[3]);
		                try{
		                    Note tmpNote = dbo.queryOneNote(NoteActivity.this, Integer.parseInt(note.getId()));
		                    if(tmpNote != null){
		                        Log.d("NoteActivity------mediaFolderName: " + tmpNote.getMediaFolderName());

		                        Log.d("NoteActivity------lastPosEnd + NoteMediaManager.TAG_SUFFIX.length(): " + lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length());
		                        // Gionee <lilg><2013-06-15> modify for CR00811864 begin
		                        //连续多个是录音文件
		                        if(posBegin == 0 || (posBegin == lastPosEnd + NoteMediaManager.TAG_STORE_SUFFIX.length() && lastPosEnd != 0)){
		                            // Gionee <lilg><2013-06-15> modify for CR00811864 end
		                            // insert midia info as a drawable into edit text
		                            Log.d("NoteActivity------here in if!");
		                            Bitmap mediaRecordBitmap = initMediaRecordBitmap(Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]), mediaInfoArray[1].substring(0, mediaInfoArray[1].indexOf(".")));
		                            insertMediaRecordIntoEditText(mediaInfoArray[1], mediaRecordBitmap, Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]));
		                        }else{

		                            Log.d("NoteActivity------here in else!");

		                            String otherStr = "";
		                            if(lastPosEnd == 0){
		                                otherStr = text.substring(lastPosEnd, posBegin);
		                            }else{
		                                otherStr = text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length(), posBegin);
		                            }

		                            noteEditText.append(otherStr.substring(otherStr.indexOf(NoteMediaManager.TAG_LARGE_STRING) + NoteMediaManager.TAG_LARGE_STRING.length()));
		                            // Gionee <lilg><2013-05-11> add for CR00809190 begin
		                            if(!otherStr.contains("\n")){
		                                noteEditText.append("\n");
		                            }
		                            // Gionee <lilg><2013-05-11> add for CR00809190 end

		                            // insert midia info as a drawable into edit text
		                            Bitmap mediaRecordBitmap = initMediaRecordBitmap(Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]), mediaInfoArray[1].substring(0, mediaInfoArray[1].indexOf(".")));
		                            insertMediaRecordIntoEditText(mediaInfoArray[1], mediaRecordBitmap, Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]));
		                        }
		                        // Gionee <lilg><2013-06-15> modify for CR00811864 begin
//		                        noteEditText.append("\n");
		                        // Gionee <lilg><2013-06-15> modify for CR00811864 end
		                    }
		                }catch(Exception e){
		                    Log.e("NoteActivity------setTextForNoteEditText exception!", e);
		                }
		            }
		        }else{
		            if(text.indexOf(NoteMediaManager.TAG_STORE_SUFFIX, lastPosEnd) == lastPosEnd){
		                noteEditText.append(text.substring(lastPosEnd + NoteMediaManager.TAG_STORE_SUFFIX.length()));
		            }else{
		                noteEditText.append(text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length()));
		            }
		            return;
		        }
		        lastPosEnd = posEnd;
		        Log.d("NoteActivity------lastPosEnd: " + posEnd);
		        // find the next  "<gionee_media:"
		        posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX, lastPosEnd);
		        Log.d("NoteActivity------posBegin: " + posBegin);
		    }

		    // 
		    Log.d("NoteActivity------lastPosEnd: " + lastPosEnd + ", text.length(): " + (text.length()));
		    if(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length() < text.length()){
		        String temp = text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length());
		        noteEditText.append(temp.substring(temp.indexOf(NoteMediaManager.TAG_LARGE_STRING) + NoteMediaManager.TAG_LARGE_STRING.length()));
		    }
		}
	}

	private void setTextForTextView(String text){
		Log.d("NoteActivity------setTextForTextView, text: " + text);
		if(showNoteContent != null){
			if(!text.contains(NoteMediaManager.TAG_PREFIX) || !text.contains(NoteMediaManager.TAG_PARSE_SUFFIX)){
				// not contains media info
				showNoteContent.setText(text);
			}else{
				// contains media info

				// clear noteTextView
				showNoteContent.setText("");
				
				// Gionee <lilg><2013-04-08> add for CR00794516 begin
                //Gionee <wangpan><2014-03-13> modify for CR01083606 begin
                //text = text.replaceAll("\n", "");
                //Gionee <wangpan><2014-03-13> modify for CR01083606 end
				// Gionee <lilg><2013-04-08> add for CR00794516 end

				int lastPosEnd = 0;
				// postion of <gionee_media:
				int posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX, lastPosEnd);
				Log.d("NoteActivity------posBegin: " + posBegin);
				while(posBegin != -1){
				    // position of />
				    int posEnd = text.indexOf(NoteMediaManager.TAG_PARSE_SUFFIX, posBegin);
				    Log.d("NoteActivity------posEnd: " + posEnd);
				    if(posEnd != -1){
				        //0:20140101005334.mp3:00:06/>>
				        String[] mediaInfoArray = text.substring(posBegin + NoteMediaManager.TAG_PREFIX.length(), posEnd).split(NoteMediaManager.TAG_MIDDLE);
				        if(mediaInfoArray != null){
				            Log.d("NoteActivity------mediaInfoType: " + mediaInfoArray[0] + ", mediaInfoId: " + mediaInfoArray[1] + ", mediaInfoMinute: " + mediaInfoArray[2] + ", mediaInfoSecond: " + mediaInfoArray[3]);
				            try{
				                Note tmpNote = dbo.queryOneNote(NoteActivity.this, Integer.parseInt(note.getId()));
				                if(tmpNote != null){
				                    Log.d("NoteActivity------mediaFolderName: " + tmpNote.getMediaFolderName());

				                    Log.d("NoteActivity------lastPosEnd + NoteMediaManager.TAG_SUFFIX.length(): " + lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length());
				                    // Gionee <lilg><2013-06-15> modify for CR00811864 begin
				                    if(posBegin == 0 || (posBegin == lastPosEnd + NoteMediaManager.TAG_STORE_SUFFIX.length() && lastPosEnd != 0)){
				                        // Gionee <lilg><2013-06-15> modify for CR00811864 end
				                        // insert midia info as a drawable into edit text
				                        Log.d("NoteActivity------here in if!");

				                        Bitmap mediaRecordBitmap = initMediaRecordBitmap(Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]), mediaInfoArray[1].substring(0, mediaInfoArray[1].indexOf(".")));
				                        insertMediaRecordIntoTextView(mediaInfoArray[1], mediaRecordBitmap, Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]));

				                    }else{

				                        Log.d("NoteActivity------here in else!");

				                        String otherStr = "";
				                        if(lastPosEnd == 0){
				                            otherStr = text.substring(lastPosEnd, posBegin);
				                        }else{
				                            otherStr = text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length(), posBegin);
				                        }
				                        Log.i("otherStr: "+otherStr);
				                        showNoteContent.append(otherStr.substring(otherStr.indexOf(NoteMediaManager.TAG_LARGE_STRING) + NoteMediaManager.TAG_LARGE_STRING.length()));
				                        // Gionee <lilg><2013-05-11> add for CR00809190 begin
				                        //											showNoteContent.append("\n");
				                        // Gionee <lilg><2013-05-11> add for CR00809190 end
				                        // Gionee <wangpan>
				                        if(!otherStr.endsWith("\n")){
				                            showNoteContent.append("\n");
				                        }
				                        // insert midia info as a drawable into edit text
				                        Bitmap mediaRecordBitmap = initMediaRecordBitmap(Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]), mediaInfoArray[1].substring(0, mediaInfoArray[1].indexOf(".")));
				                        insertMediaRecordIntoTextView(mediaInfoArray[1], mediaRecordBitmap, Integer.parseInt(mediaInfoArray[2]), Integer.parseInt(mediaInfoArray[3]));
				                    }
				                    // Gionee <lilg><2013-06-15> modify for CR00811864 begin
				                    showNoteContent.append("\n");
				                    // Gionee <lilg><2013-06-15> modify for CR00811864 end
				                }
				            }catch(Exception e){
				                Log.e("NoteActivity------setTextForTextView exception!", e);
				            }
				        }
				    }else{
				        if(text.indexOf(NoteMediaManager.TAG_STORE_SUFFIX, lastPosEnd) == lastPosEnd){
				            showNoteContent.append(text.substring(lastPosEnd + NoteMediaManager.TAG_STORE_SUFFIX.length()));
				        }else{
				            showNoteContent.append(text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length()));
				        }
				        return;
				    }
				    lastPosEnd = posEnd;
				    Log.d("NoteActivity------lastPosEnd: " + posEnd);
				    posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX, lastPosEnd);
				    Log.d("NoteActivity------posBegin: " + posBegin);
				};
				
				// after the media file, it has other words
				Log.d("NoteActivity------lastPosEnd: " + lastPosEnd + ", text.length(): " + (text.length()));
				if(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length() < text.length()){
					String temp = text.substring(lastPosEnd + NoteMediaManager.TAG_PARSE_SUFFIX.length());
					showNoteContent.append(temp.substring(temp.indexOf(NoteMediaManager.TAG_LARGE_STRING) + NoteMediaManager.TAG_LARGE_STRING.length()));
				}
			}
		}
	}

	private void initNoteEditTextPosMap(String text) {
		if (noteEditTextPosMap == null) {
			noteEditTextPosMap = new HashMap<Integer, Integer>();
		} else {
			noteEditTextPosMap.clear();
		}

		int lastPosEnd = 0;
		int posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX, lastPosEnd);
		Log.d("NoteActivity------posBegin: " + posBegin);
		if (posBegin != -1) {
			do {
				int posEnd = text.indexOf(NoteMediaManager.TAG_PARSE_SUFFIX,
						posBegin);
				Log.d("NoteActivity------posEnd: " + posEnd);
				if (posEnd != -1) {

					noteEditTextPosMap.put(posBegin, 0);
					noteEditTextPosMap.put(posEnd
							+ NoteMediaManager.TAG_PARSE_SUFFIX.length(), 1);

					int posEnd2 = text.indexOf(
							NoteMediaManager.TAG_STORE_SUFFIX, posBegin);
					if (posEnd2 != -1) {
						noteEditTextPosMap
								.put(posEnd2
										+ NoteMediaManager.TAG_STORE_SUFFIX
												.length(), 1);
					}

					lastPosEnd = posEnd;
					Log.d("NoteActivity------lastPosEnd: " + posEnd);
					posBegin = text.indexOf(NoteMediaManager.TAG_PREFIX,
							lastPosEnd);
					Log.d("NoteActivity------posBegin: " + posBegin);
				}else{
					return;
				}
			} while (posBegin != -1);
		}
	}

	public class RecordThread extends Thread {
		private MediaRecorder mRecorder = null;
		private ProgressBar progressbar = null;
		private int bs;
		private static final int SAMPLE_RATE_IN_HZ = 8000;
		private static final int MAX_VOICE_VALUE = 100;

		public RecordThread(MediaRecorder mRecorder, ProgressBar progressbar) {
			super();
			this.mRecorder = mRecorder;
			this.progressbar = progressbar;
		}

		public void run() {
			try {
				super.run();
				while (isRun) {
					if (mRecorder == null) {
						break;
					}
					int amplitude = mRecorder.getMaxAmplitude();
					int value = MAX_VOICE_VALUE * amplitude / 32768;
//					Log.v("NoteActivity---RecordThread---" + value);
					for (int i = 0; i <= value; i++) {
						progressbar.setProgress(value);
						if (i != value) {
							if (value != 0) {
								sleep(progressTime / value);
							}
						}
					}
					// progressbar.setProgress(value);
					// sleep(400);
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("NoteActivity---RecordThread---" + e);
			}
		}
	}

	private void startProgress() {
		MediaRecorder mRecorder = mMediaManager.getmMediaRecorder();
		RecordThread thread = new RecordThread(mRecorder,
				mProgressBarMediaRecord);
		isRun = true;
		thread.start();
	}

	private void stopProgress() {
		isRun = false;
	}
	
	private int getMediaRecordNum(Context context, String noteId){
		if(dbo == null){
			dbo = DBOperations.getInstances(NoteActivity.this);
		}
		return dbo.queryMediaRecordNumByNoteId(context, noteId);
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void enterIntoEdit(Note note) {
	    if(null == note){
	        return;
	    }
		try {
			Log.i("NoteActivity------onSingleTapUp!");
			Statistics.onEvent(NoteActivity.this, Statistics.NOTE_EDIT);
			// gn pengwei 20121126 add for statistics end
			browseView.setVisibility(View.GONE);
			noteTopEdit.setVisibility(View.VISIBLE);
			noteBottom.setVisibility(View.VISIBLE);
			// cao
			if (noteAttchmentItem != null) {
				noteAttchmentItem.setImageResource(R.drawable.note_attachment);
			}
			enableLabel();
			// gn pengwei 20130227 add for CR00774114 begin
			mCustomView.setEnabled(true);
			// gn pengwei 20130227 add for CR00774114 end
			noteEditText.requestFocus();
			InputMethodManager inputMethodManager = (InputMethodManager) NoteActivity.this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			setTextForNoteEditText(note.getContent());
			
			if (note.getAlarmTime() == null
			        || Constants.INIT_ALARM_TIME
			        .equals(note.getAlarmTime())) {
			    hideAlarm();
			} else {
			    showAlarm(note);
			}
			if (note.getAddressName() == null
			        || "".equals(note.getAddressName())) {
			    hideAddress();
			} else {
			    showAdress(note.getAddressName());
			}
			
			String timeShowStr = CommonUtils.getNoteData(this,
					note.getUpdateDate(), note.getUpdateTime());
			timeShow.setText(timeShowStr);
			initNoteScreen(note);
			changeTextColor(note);
			inputMethodManager.showSoftInput(noteEditText, 0);
			noteEditText.setSelection(noteEditText.getText().toString().length());
			responseGesture = false;
			isResponseGesture(true);
			initMenu();
		} catch (Exception e) {
			Log.e("enterIntoEdit Exception:"+e.getMessage());
		}
	}
	
	private void initNewNote(){
		changeTextColor(note);
		String title = note.getTitle() != null && !"".equals(note.getTitle()) ? note
				.getTitle() : getResources().getString(
				R.string.note_new_title_label);
        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 begin
	    //Gionee <wangpan><2014-05-21> modify for CR01268739 begin
		//mCustomView.setText(CommonUtils.getTitleString(title));
        mCustomView.setText(getTitleString(title));
	    //Gionee <wangpan><2014-05-21> modify for CR01268739 end
        //Gionee <wanghaiyan><2015-03-10> modify for CR01451728 end
		updateView();
		updateAddress(note.getAddressName());

		String timeShowStr = CommonUtils.getNoteData(this,
				note.getUpdateDate(), note.getUpdateTime());
		timeShow.setText(timeShowStr);
	}
	
	private AmigoAlertDialog alertDialog;
	private void menuOnClick(){
		initMenuAndAttachment();
			alertDialog = new AmigoAlertDialog.Builder(this).setTitle(this.getResources().getString(R.string.note_detail_menu)).setItems(
					mMenuItem, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							itemSelected(which);
						}
					}).create();
			alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();
	}
	
	private boolean menuIsShowing(){
		Log.v("menuisshow == ");
		if(alertDialog == null){
			return false;
		}
		Log.v("menuisshow == " + alertDialog.isShowing());
		return alertDialog.isShowing();
	}
	
	private void menuDissmiss(){
		if(alertDialog == null){
			return;
		}
		alertDialog.dismiss();
	}
	
	//Gionee <pengwei><20130814> modify for CR00856002 begin

	private boolean itemSelected(int which) {
		if(which == NOTE_DEL){

			handler.removeMessages(SHOWVIEWPANEL);
			// gn pengwei 20121126 add for statistics begin
			Statistics.onEvent(NoteActivity.this, Statistics.NOTE_DELNOTE);
			// gn pengwei 20121126 add for statistics end
			showDeleteDialog();
		}else if(which == NOTE_CHANGE_BG){
			changeBg();
			Intent intent = new Intent(NoteActivity.this,
					GuideGalleryActivity.class);
			try {
				intent.putExtra("bgColor", Integer.parseInt(note.getBgColor()));
			} catch (Exception e) {
				// TODO: handle exception
				Log.i("NoteActivity---note_change_bgcolor---e == " + e);
				intent.putExtra("bgColor",0);
			}
			startActivityForResult(intent, GUIDEGALLERYACTIVITYCODE);
		}else if(which == NOTE_SEND_TO_LAUNCH){
			Log.i("NoteActivity------note send launcher begin!");
			Log.i("NoteActivity------note.getId(): "
					+ (note.getId())
					+ ", noteEditText length: "
					+ (noteEditText.getText().length())
					+ ", note.getWidgetId(): "
					+ note.getWidgetId()
					+ ", "
					+ (noteEditText.getText().length() > 0 || note.getTitle() != null)
					+ ", note.getTitle().length(): "
					+ (note.getTitle() == null));
			sendToLaunch();
			// Gionee <pengwei><2013-4-8> modify for CR00794211 begin
			if (responseGesture) {
				if (note != null) {
					if ((note.getContent() != null && note.getContent()
							.length() > 0) || note.getTitle() != null) {
						if (!note.getWidgetId().equals("-1")) {
							Log.i("NoteActivity------33333");
							CommonUtils
									.showToast(
											NoteActivity.this,
											getResources()
													.getString(
															R.string.note_send_luncher_haved_toast));
						} else {
								Log.i("NoteActivity------44444");
								Intent intentSendLaunch = new Intent(
										Constants.SEND_LUNCHER);
								Bundle data = new Bundle();
								data.putString(
										Constants.KEY_REQUEST_PACKAGE_NAME,
										Constants.PACKAGE_NAME);
								data.putString(
										Constants.KEY_REQUEST_CLASS_NAME,
										Constants.CLASS_NAME);
								data.putString(Constants.KEY_REQUEST_FALG,
										note.getId());
								intentSendLaunch.putExtras(data);
								NoteActivity.this
										.sendBroadcast(intentSendLaunch);
								UtilsQueryDatas.changeNote(note,
										HomeActivity.mTempNoteList);
						}
					} else {
						Log.i("NoteActivity------11111");
						CommonUtils.showToast(
								NoteActivity.this,
								getResources().getString(
										R.string.note_not_send_luncher_toast));
					}

				}
			} else {
				if (noteEditText.getText().length() > 0
						|| note.getTitle() != null) {
					if (!note.getWidgetId().equals("-1")) {
						Log.i("NoteActivity------33333");
						CommonUtils
								.showToast(
										NoteActivity.this,
										getResources()
												.getString(
														R.string.note_send_luncher_haved_toast));
					} else {

						if (note.getId() == null) {
							Log.i("NoteActivity------22222");
							String content = noteEditText.getText() != null ? noteEditText
									.getText().toString().trim()
									: "";
							note.setContent(content);
							// gn pengwei 20121218 add for
							// CR00748107 begin
							note.setUpdateDate(dbo.getDate());
							note.setUpdateTime(dbo.getTime());
							// gn pengwei 20121218 add for
							// CR00748107 begin
							refreshAlarm(note);
							long noteId = dbo.createNote(NoteActivity.this,
									note);
							note.setId(String.valueOf(noteId));
							// gn pengwei 20130122 modify for CR00766172 begin
							// _id = new Long(noteId).intValue();
							// gn pengwei 20130122 modify for CR00766172 end
							CommonUtils.noteID = note.getId();
							Intent intentSendLaunch = new Intent(
									Constants.SEND_LUNCHER);
							Bundle data = new Bundle();
							data.putString(Constants.KEY_REQUEST_PACKAGE_NAME,
									Constants.PACKAGE_NAME);
							data.putString(Constants.KEY_REQUEST_CLASS_NAME,
									Constants.CLASS_NAME);
							data.putString(Constants.KEY_REQUEST_FALG,
									note.getId());
							intentSendLaunch.putExtras(data);
							NoteActivity.this.sendBroadcast(intentSendLaunch);
							UtilsQueryDatas.addNote(note,
									HomeActivity.mTempNoteList);
						} else {
							Log.i("NoteActivity------44444");
							Intent intentSendLaunch = new Intent(
									Constants.SEND_LUNCHER);
							Bundle data = new Bundle();
							data.putString(Constants.KEY_REQUEST_PACKAGE_NAME,
									Constants.PACKAGE_NAME);
							data.putString(Constants.KEY_REQUEST_CLASS_NAME,
									Constants.CLASS_NAME);
							data.putString(Constants.KEY_REQUEST_FALG,
									note.getId());
							intentSendLaunch.putExtras(data);
							NoteActivity.this.sendBroadcast(intentSendLaunch);
							UtilsQueryDatas.changeNote(note,
									HomeActivity.mTempNoteList);
						}
					}
				} else {
					Log.i("NoteActivity------11111");
					CommonUtils.showToast(NoteActivity.this, getResources()
							.getString(R.string.note_not_send_luncher_toast));
				}
			}
			// Gionee <pengwei><2013-4-8> modify for CR00794211 end
			Log.i("NoteActivity------note send launcher end!");
		}else if(which == NOTE_SHARE){
			   if (!responseGesture) {
				      if (noteEditText.getText().length() < 1) {
				          // gionee 20121204 jiating modify for CR00739157 begin
				          // noteShare.setVisible(false);
				          // gionee 20121204 jiating modify for CR00739157 end
				    	  CommonUtils.showToast(NoteActivity.this, getResources().getString(R.string.empty_note_not_send_luncher_toast));
				    	  return true;
				      } 
				  }
			shareNote();
		}
		return true;
	}
	
	
	//Gionee <pengwei><20130814> modify for CR00856002 end
	//Gionee <pengwei><20130823> modify for CR00869441 end
	
	//Gionee <pengwei><2013-12-10> modify for CR00972801 begin
    public boolean isNaviLauncher() {
        String packageName = "com.gionee.navil";
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = this.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		String currentHomePackage = resolveInfo.activityInfo.packageName;
		if ((packageName.equals(currentHomePackage))  ||( "android".equals(currentHomePackage))) {		
			return true;
		}
	    return false;
	}
	//Gionee <pengwei><2013-12-10> modify for CR00972801 end
    // Gionee <wangpan><2014-08-15> add for CR01357172 begin
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("note", note);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        note = (Note) savedInstanceState.getSerializable("note");
    }
    // Gionee <wangpan><2014-08-15> add for CR01357172 end

	private void deleteMediaFromList(String delTextInfo) {
		List<String> mediaInfoList = CommonUtils.getMediasFromNoteContent(delTextInfo);
		if(mediaInfoList != null && mediaInfoList.size() > 0){
			for(String mediaInfo : mediaInfoList){					
			Log.d("NoteActivity------delete mediaInfo: " + mediaInfo);
				if (!TextUtils.isEmpty(mediaInfo)) {
				String[] mediaInfos = mediaInfo.split(NoteMediaManager.TAG_MIDDLE);
					if (mediaInfos != null) {
					String mediaType = mediaInfos[0];

					String mediaFileName = mediaInfos[1];
					Log.d("NoteActivity------delete mediaInfo type: " + mediaType + ", mediaFileName: " + mediaFileName);

					// delete the media file in the sdcard
					Note tmpNote = DBOperations.getInstances(NoteActivity.this).queryOneNote(NoteActivity.this, Integer.parseInt(note.getId()));
					//								String fileToDelete = tmpNote.getMediaFolderName() + "/" + mediaFileName;

						if(!TextUtils.isEmpty(tmpNote.getMediaFolderName())){

						// Gionee <lilg><2013-04-17> add for CR00795461 begin
						//									String fileToDelete = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(tmpNote.getMediaFolderName()) + "/" + mediaFileName;
						String fileToDelete = FileUtils.getPathByPathType(tmpNote.getMediaFolderName().substring(0, 1)) + FileUtils.getSubPath(NoteActivity.this, tmpNote.getMediaFolderName()) + "/" + mediaFileName;
						// Gionee <lilg><2013-04-17> add for CR00795461 end

						Log.d("NoteActivity------the media file to delete: " + fileToDelete);

						// the media in the backup dir will not to be delete.
						if(fileToDelete.contains(getResources().getString(R.string.path_note_media))){
						File mediaFile = new File(fileToDelete);
							if(mediaFile.exists()){
								boolean isDelete = mediaFile.delete();
								Statistics.onEvent(NoteActivity.this, Statistics.NOTE_DEL_RECORD);
								Log.d("NoteActivity------media file is delete: " + isDelete);
							}
						}

						// delete the media item in db
						DBOperations.getInstances(NoteActivity.this).deleteMediaByFileName(NoteActivity.this, tmpNote.getMediaFolderName() + "/" + mediaFileName);

						// stop playing
							if(mMediaManager != null && mMediaManager.isPlaying() && mMediaManager.getmMediaItemFileNameCurrentPlaying().equals(mediaFileName)){
								if(handler != null){
									handler.sendEmptyMessage(FLAG_STOP_PLAYER_PLAYING);
								}
							}
						}
					}
				}
			}
		}

	}
	private class MediaImageHandler implements TextWatcher {

        private final NoteEditText mEditor;
        private final ArrayList<ImageSpan> mMediaImagesToRemove = new ArrayList<ImageSpan>();
		private String temp;// Temporary storage before the input string
		private int selectionStart;// the start position of the cursor

        public MediaImageHandler(NoteEditText editor) {
            // Attach the handler to listen for text changes.
            mEditor = editor;
            mEditor.addTextChangedListener(this);
        }
		
		private void checkNoteContent(Editable noteContent) {
			if (null != noteContent  && noteContent.length() > CONTENT_MAX_LENGTH) {
				Toast.makeText(NoteActivity.this,getString(R.string.note_content_max_length),
				Toast.LENGTH_SHORT).show();
				// The new join string into cursor back and prevent new
				// input string cover in front of the string
				int tempLen = temp.length();
				CharSequence tempChar = noteContent.subSequence(
				selectionStart,
				selectionStart + noteContent.length() - tempLen);
				CharSequence addChar = tempChar.subSequence(0,
				CONTENT_MAX_LENGTH - tempLen);
				String tempBef = temp.substring(0, selectionStart);
				String tempAft = temp.substring(selectionStart, tempLen);
				String showStr = tempBef + addChar + tempAft;
			
				// Gionee <lilg><2013-03-29> modify CR00791333 begin
				//					noteEditText.setText(showStr);
				setTextForNoteEditText(showStr);
				// Gionee <lilg><2013-03-29> modify CR00791333 end
				// Gionee <wangpan><2014-11-20> modify for CR01412725 begin
				noteEditText.setSelection(CONTENT_MAX_LENGTH);
				// Gionee <wangpan><2014-11-20> modify for CR01412725 end
			}
			
			Log.v("NoteActivity--wpeng-----------" + noteContent);
			String sStr = "";
			if (noteContent != null) {
				sStr = noteContent.toString();
			} 
			// Gionee <wangpan><2014-08-18> modify for CR01316171 begin 
			if ("".equals(sStr.trim())) {
				// Gionee <wangpan><2014-08-18> modify for CR01316171 end 
				CommonUtils.isAbledAdd(NoteActivity.this, createNoteItem,false);
				CommonUtils.isAbledAdd(NoteActivity.this, shareItem, false);
			} else {
				CommonUtils.isAbledAdd(NoteActivity.this, createNoteItem,true);
				CommonUtils.isAbledAdd(NoteActivity.this, shareItem, true);
			}
		}

        public void insert(String MediaImage, int resource) {
            // Create the ImageSpan
            Drawable drawable = mEditor.getResources().getDrawable(resource);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

            // Get the selected text.
            int start = mEditor.getSelectionStart();
            int end = mEditor.getSelectionEnd();
            Editable message = mEditor.getEditableText();

            // Insert the MediaImage.
            message.replace(start, end, MediaImage);
            message.setSpan(span, start, start + MediaImage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            // Check if some text will be removed.
			temp = mEditor.getText().toString();// Get before the input
			// string
			selectionStart = mEditor.getSelectionStart();
            if (count > 0) {
                int end = start + count;
                Editable message = mEditor.getEditableText();
                ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);

                for (ImageSpan span : list) {
                    // Get only the MediaImages that are inside of the changed
                    // region.
                    int spanStart = message.getSpanStart(span);
                    int spanEnd = message.getSpanEnd(span);
                    if ((spanStart < end) && (spanEnd > start)) {
                        // Add to remove list
                        mMediaImagesToRemove.add(span);
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable text) {
            Editable message = mEditor.getEditableText();

            // Commit the MediaImages to be removed.
            for (ImageSpan span : mMediaImagesToRemove) {
                int start = message.getSpanStart(span);
                int end = message.getSpanEnd(span);
				CharSequence myS=message.toString();

                // Remove the span
                message.removeSpan(span);

                // Remove the remaining MediaImage text.
                if (start != end) {
					CharSequence delCharSequence = message.subSequence(start, end);
                    message.delete(start, end);
					NoteActivity.this.deleteMediaFromList(delCharSequence.toString());
                }
            }
            mMediaImagesToRemove.clear();					
        	checkNoteContent(text);
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
        }

    }
}
