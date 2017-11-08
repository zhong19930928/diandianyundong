package com.fox.exercise.map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.fox.exercise.R;
import com.fox.exercise.SmartBarUtils;
import com.fox.exercise.SportsType;
import com.fox.exercise.api.AddCoinsThread;
import com.fox.exercise.api.ApiBack;
import com.fox.exercise.api.ApiConstant;
import com.fox.exercise.api.ApiJsonParser;
import com.fox.exercise.api.ApiNetException;
import com.fox.exercise.api.ApiSessionOutException;
import com.fox.exercise.api.QQHealthTask.QQHealthResult;
import com.fox.exercise.api.entity.AddFindItem;
import com.fox.exercise.api.entity.GetPeisu;
import com.fox.exercise.api.entity.PeisuInfo;
import com.fox.exercise.db.PeisuDB;
import com.fox.exercise.db.SportSubTaskDB;
import com.fox.exercise.http.FunctionStatic;
import com.fox.exercise.lockscreen.SliderRelativeLayout;
import com.fox.exercise.login.LocationInfo;
import com.fox.exercise.newversion.UUIDGenerator;
import com.fox.exercise.newversion.act.MyFirstSportFragment;
import com.fox.exercise.newversion.bushutongji.BuShuTongJiDB;
import com.fox.exercise.newversion.entity.ScreenListener;
import com.fox.exercise.newversion.entity.SportsMarkDis;
import com.fox.exercise.newversion.newact.SportsHonorActivity;
import com.fox.exercise.newversion.newact.SportstingSettingActivity;
import com.fox.exercise.pedometer.BadmintonUtils;
import com.fox.exercise.pedometer.FootballUtils;
import com.fox.exercise.pedometer.GolfingUtils;
import com.fox.exercise.pedometer.MountainUtils;
import com.fox.exercise.pedometer.RidingUtils;
import com.fox.exercise.pedometer.RollerSportsUtils;
import com.fox.exercise.pedometer.RowingUtils;
import com.fox.exercise.pedometer.RunningUtils;
import com.fox.exercise.pedometer.SkatingUtils;
import com.fox.exercise.pedometer.SwimmingUtils;
import com.fox.exercise.pedometer.TennisUtils;
import com.fox.exercise.pedometer.TimingManager;
import com.fox.exercise.pedometer.WalkingRaceUtils;
import com.fox.exercise.pedometer.WalkingUtils;
import com.fox.exercise.publish.Bimp;
import com.fox.exercise.publish.SendMsgDetail;
import com.fox.exercise.util.SportTaskUtil;
import com.fox.exercise.util.SportTrajectoryUtilGaode;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.ingenic.indroidsync.SportsApp;
import cn.ingenic.indroidsync.utils.DeviceUuidFactory;

/**
 * @author loujungang 高德地图运动页面
 */
public class SportingMapActivityGaode extends Activity implements
        SensorEventListener, OnClickListener, LocationSource,
        AMapLocationListener, TabListener {

    private Activity mContext;
    private static final String TAG = "SportingMapActivityGaode";

    private MapView mMapView = null;
    private AMap aMap;
    private UiSettings mUiSettings;
    public static final int CLICK_SPORT_IMG = 0;
    public static final int LOCKSCREEN_DELAY = 10 * 1000;

    private final String SERVICE_NAME = "com.fox.exercise.pedometer.STEPSERVICE";

    private LatLng locData = null;//当前定位的点
    private LatLng curLocData = null;
    private LatLng curMediaLoc = null;

    private static final int DRAWTRAJECTORY = 1;
    private static final int SPEEDLIMITNUM = 5;
    private static  final int YUYINBOBAONUM=6;
    public double mLat = 0;
    public double mLng = 0;
    private List<LatLng> mGeoPoints = new ArrayList<LatLng>();
    private List<LatLng> mDrawPoints = new ArrayList<LatLng>();
    private boolean lastPointInValid = false;

    private TextView chron, time_txt;
    private LinearLayout startStopBut;
    private ImageButton backBtn;
    private Button  restartBtn, stopBtn;
    private Timer timer;
    private TimerTask task;
    private boolean isPauseForGPS = false;// GPS信号丢失暂停
    private boolean isRun = false;

    private RelativeLayout unfoldView;
    private ImageButton unfoldBtn,unfoldBtn2;// , resumeBtn;
    private boolean isGone = false;
    private TextView altitudeValue,
            pressureValue;
    private ImageView imageGps, imageview_jibu;

    private String startTime, todayTime;
    private long startTimeSeconds;
    private String tempAltitude;
    private int limitId, uid;
    private int typeId, deviceId;
    private int typeDetailId = 0;
    private double pace;// 配速

    private int isUpload = 0;

    private double dis, speed, otherSpeed,tempDis;// , heart;
    private double heart = 0.0;
    private double lastSpeed = 0.0;
    private double newSpeed = 0.0;
    private int maxSpeedNum = 0;
    private boolean speedOverLimit = false;
    private int con, stepNum;
    private String cityname, findString;
    private ArrayList<String> list_bitmap_path_upload = new ArrayList<String>();
    private SharedPreferences spf;
    private TextView message;
    private int endSportType;

    private static final int UPLOAD_PARAM_ERROR = 10001;
    private static final int UPLOAD_START = 10002;
    private static final int UPLOAD_UPDATE = 10003;
    private static final int UPLOAD_DELETE = 10006;
    private static final int SAVE_LOCAL = 10009;

    private static final int UPDATE_STEP = 1;
    private static final int UPDATE_LOCKSCREEN = 2;
    private static final int GO_PAUSE = 3;

    public static int LOCKSCREEN = 0x04;

    private Dialog mLoadDialog = null;
    private Dialog mUploadDialog = null;
    public int taskID = 0;
    private boolean isMedia = false;

    private Editor ed;
    private AddCoinsHandler addCoinsHandler = null;
    private SportSubTaskDB db;

    private MarkerOptions mMarkerOpStart;
    private MarkerOptions mMarkerOpMiddle;

    private Marker mMarkerStart = null;
    private Marker mMarkerMiddle = null;

    private SharedPreferences sp;
    private SharedPreferences sharedPreferences;
    private int isBack;
    private Boolean isStart = false;
    private int sportype = 1;
    private SportsApp mSportsApp;

    // 定义LocationManager对象
    private LocationManager locationManager = null;
    private GpsLocationListener gpsLocationListener = null;

    private Dialog alertDialog;

    private long preTime = 0;

    // 锁屏
    private SliderRelativeLayout sliderLayout = null;
    public static int MSG_LOCK_SUCESS = 1;
    private SharedPreferences foxSportSetting,voiceSportSetting;
    private RelativeLayout lockscreen;
    private Map<String, String> sporttype;
    private WakeLock mWakeLock;

    private Boolean isFirstSave = true;
    private Boolean isEndSave = false;

    private PolylineOptions options;
    private Polyline mPolyline;

     private AMapLocationClient mapLocationClient;
     private AMapLocationClientOption mapLocationClientOption;
    private float zoomValue = 18;

    private boolean findMethod;

    private TimingManager timingMgr;
    private int coins; // 要上传的金币数
    private boolean activityPause = false;
    private boolean canSave = false;
    // 上传成功和更新成功后返回的
    private ApiBack theApiBack = null;
    private int thereturn;
    private boolean is_start_map = true;
    // 语音提示
    private VoicePrompt vp;
    private Map<String, String> hashMap = new HashMap<String, String>();
    private SensorManager sensorManager;
    private Sensor ss;
    private float accelerometer;
    private Boolean mTroughAppear = false;
    private int mCountNum;
    private long mPrevStepTime;
    private int lastCountNum = 10;


    private String conStringValue, speedStringValue,
            paceStringValue;
    private TextView pop_chronometerId;

    private TextView sporting_sport_peiPace, sporting_sport_pingjunSpeed,
            sporting_sportxiaohao, time_disValue, zong_li_txt;
    private Button suodingBtn;

    private ImageButton sporting_setting_icon;// 
    private boolean isVoiceON;// 判断语音是否打开关闭


    private boolean mIsFirst = false;// 程序是否运行的标志位

    private TextView sport_topTitle;


    private String markCode;// 运动唯一标识

    private double temDis;//jps和记录模式来回切换保存的几步模式距离

    private RelativeLayout mPopMenuBack;
    private LinearLayout pesulin;
    private PeisuDB peisuDB;
    private PeisuInfo mPeiInfo;
    private ArrayList<String> peiSuList1, peiSuList2, peiSuList3;//配速的三个集合
    private ArrayList<String> peiSuList4;//表示登山是否有gps
    private boolean savePeisuFirst = true;//配速是不是第一次保存
    private int climbHeight = 0;//爬山高度
    private int gps_type=1;
    private LinearLayout sport_ll_lock;
    private boolean islock = true;
    private ScreenListener screenListener;// 屏幕锁定监控
    private HomeWatcherReceiver homeWatcherReceiver;
    private boolean isMapQiehuan=false;//地图是否切换
    private ArrayList<String> speedList;//表示速度列表
    private int stepsInDB = -1;
    private ArrayList<SportsMarkDis> sportsMarkDisList=new ArrayList<SportsMarkDis>();
    private ImageButton sporting_setting_btn;
    private int playDisNum=0,playTimeNum=0;
    private String grouptype;//标示是否达到勋章的标示
    private String pointString;//经纬度的点的字符串
    private String strSpeedList,strSportsMarkDisList;
    private double avg_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findMethod = SmartBarUtils.findActionBarTabsShowAtBottom();
        if (!findMethod) {
            // 取消ActionBar拆分，换用TabHost
            getWindow().setUiOptions(0);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.sporting_map_gaode);
        mPeiInfo = new PeisuInfo();
        mContext = this;
        mSportsApp = (SportsApp) getApplication();
        mSportsApp.isStartY = false;
        mSportsApp.addActivity(this);
        // 友盟推送
        PushAgent.getInstance(this).onAppStart();
        mSportsApp.setmSettingHandler(handler);


        mMapView = (MapView) mContext.findViewById(R.id.bmapView);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写

        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            mUiSettings = aMap.getUiSettings();
            mUiSettings.setZoomControlsEnabled(false);
            mUiSettings.setZoomGesturesEnabled(true);
            mUiSettings.setMyLocationButtonEnabled(false); // 是否显示默认的定位按钮

            aMap.setLocationSource(this);// 设置定位监听
            aMap.setMyLocationEnabled(true);// 是否可触发定位并显示定位层
            aMap.setOnMapLoadedListener(new OnMapLoadedListener() {
                @Override
                public void onMapLoaded() {
                    if (mLoadDialog!=null&&mLoadDialog.isShowing()) {
                        mLoadDialog.dismiss();
                    }
                }
            });// 设置amap加载成功事件监听器

            aMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    return false;
                }
            });// 设置点击marker事件监听器

            aMap.setOnMapTouchListener(new OnMapTouchListener() {
                @Override
                public void onTouch(MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
//                            RemoveLockScreenMSG();
                            break;
                        case MotionEvent.ACTION_UP:
//                            SendLockScreenMSG();
                            break;
                    }
                }
            });
        }
        initGPS();

        mMarkerOpStart = new MarkerOptions().icon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.map_start)));
        mMarkerOpMiddle = new MarkerOptions().icon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.map_crulocation_icon)));

        Bundle bundle = new Bundle();
        bundle.putString("message",
                getResources().getString(R.string.sports_wait));
        mLoadDialog = onCreateDialog(1, bundle, 0);
//        mLoadDialog.show();

        Bundle bundle1 = new Bundle();
        bundle1.putString(
                "message",
                getResources().getString(
                        R.string.sports_authentication_uploading));
        mUploadDialog = onCreateDialog(1, bundle1, 0);

        initView();
        setView();
        if (findMethod) {
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(false);
            final ActionBar bar = getActionBar();
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_stop)
                    .setTabListener(this));
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_start_or_stop)
                    .setTabListener(this));

            SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
            SmartBarUtils.setActionModeHeaderHidden(bar, true);
            SmartBarUtils.setActionBarViewCollapsable(bar, true);
        }
        foxSportSetting = getSharedPreferences("sports" + uid, 0);
        voiceSportSetting = getSharedPreferences("voice_sports", 0);
        isOpen = foxSportSetting.getBoolean("lockScreen", false);
        lockisopen = false;
        Editor editor = foxSportSetting.edit();
        editor.putBoolean("lockisopen", lockisopen);
        editor.commit();

        isVoiceON = voiceSportSetting.getBoolean("voiceon", true);
        if(isVoiceON){
            playdis=voiceSportSetting.getInt("playdis",0);
            playtime=voiceSportSetting.getInt("playtime",0);
            if(playdis<=0&&playtime<=0){
                playdis=1;
                voiceSportSetting.edit().putInt("playdis",1).commit();
            }
        }
        addCoinsHandler = new AddCoinsHandler();

        timingMgr = TimingManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter
                .addAction("com.fox.exercise.pedometer.TimerReceiver.alarmclock");
        registerReceiver(timeReceiver, intentFilter);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getCanonicalName());

        sensorManager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);
        ss = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (is_start_map) {
            if ((locData != null && gpsType > 0) || (null != ss)) {
                mSportsApp.isRunning=true;
                goStart();
                is_start_map = false;
            } else {
                if (gpsType > 0) {
                    Toast.makeText(mContext,
                            getString(R.string.in_gps_locating),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext,
                            getString(R.string.location_gps_weak),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        screenListener = new ScreenListener(mContext);
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                //开屏
            }

            @Override
            public void onScreenOff() {
                //锁屏
//                if (mWakeLock != null && !mWakeLock.isHeld()) {
//                    mWakeLock.acquire();
//                }
            }

            @Override
            public void onUserPresent() {
                //解锁
            }
        });

        registerHomeKeyReceiver(this);

    }

    private void initView() {
        spf = getSharedPreferences("sports", 0);
        cityname = spf.getString("cityname", "");
        backBtn = (ImageButton) mContext.findViewById(R.id.sport_map_back);
        backBtn.setOnClickListener(this);

        unfoldView = (RelativeLayout) mContext
                .findViewById(R.id.startingLayout);
        unfoldView.getBackground().setAlpha(200);

        unfoldBtn = (ImageButton) mContext.findViewById(R.id.bigBtn);
        unfoldBtn.setOnClickListener(this);
        unfoldBtn2 = (ImageButton) findViewById(R.id.bigBtn2);
        unfoldBtn2.setOnClickListener(this);

        chron = (TextView) mContext.findViewById(R.id.chronometerId);
        time_txt = (TextView) mContext.findViewById(R.id.time_txt);

        restartBtn = (Button) mContext.findViewById(R.id.restartBtn);
        restartBtn.setOnClickListener(this);
        stopBtn = (Button) mContext.findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(this);

        sliderLayout = (SliderRelativeLayout) findViewById(R.id.slider_layout);
        lockscreen = (RelativeLayout) findViewById(R.id.lockscreen);
        startStopBut = (LinearLayout) findViewById(R.id.start_stop_but);
        if (findMethod) {
            startStopBut.setVisibility(View.GONE);
        }
        imageGps = (ImageView) findViewById(R.id.imageview_gps);
        imageview_jibu = (ImageView) findViewById(R.id.imageview_jibu);

        altitudeValue = (TextView) findViewById(R.id.textview_haiba);
        pressureValue = (TextView) findViewById(R.id.textview_qiya);

        sport_ll_lock = (LinearLayout) findViewById(R.id.sport_ll_lock);
        sporting_sport_peiPace = (TextView) findViewById(R.id.sporting_sport_peiPace);
        sporting_sport_pingjunSpeed = (TextView) findViewById(R.id.sporting_sport_pingjunSpeed);
        sporting_sportxiaohao = (TextView) findViewById(R.id.sporting_sportxiaohao);
        time_disValue = (TextView) findViewById(R.id.time_disValue);
        zong_li_txt = (TextView) findViewById(R.id.zong_li_txt);


        sporting_setting_btn = (ImageButton) findViewById(R.id.sporting_setting_btn);
        sporting_setting_btn.setOnClickListener(this);

        sport_topTitle = (TextView) findViewById(R.id.sport_topTitle);
        mPopMenuBack = (RelativeLayout) findViewById(R.id.set_menu_background);
        pesulin = (LinearLayout) findViewById(R.id.lin_peisu);

        findViewById(R.id.maptype_qiehuan).setOnClickListener(this);
    }


    private void setView() {
        uid = mSportsApp.getSportUser().getUid();
        sp = mContext.getSharedPreferences("sport_state_" + uid, 0);
        sharedPreferences = getSharedPreferences("sports" + uid, 0);
        ed = sp.edit();

        String latitude = LocationInfo.latitude;
        String longitude = LocationInfo.longitude;
        if (!TextUtils.isEmpty(longitude) || !TextUtils.isEmpty(longitude)) {
            double lat = Double.valueOf(latitude);
            double log = Double.valueOf(longitude);
            if (lat != 0 || log != 0) {
                try {
                    LatLng point = new LatLng(lat, log);
                    animateTo(point);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        typeId = sp.getInt("typeId", 1);
        String typeStr = null;
        if (typeId == SportsType.TYPE_SWIM) {
            typeDetailId = sp.getInt("typeDetailId", 0);
            typeStr = getString(SportTaskUtil.getDetailTypeName(typeId,
                    typeDetailId));
        } else if (typeId == SportsType.TYPE_WALK
                || typeId == SportsType.TYPE_RUN) {
            typeDetailId = sp.getInt("typeDetailId", 0);
            if (typeDetailId == 0) {
                // 打开GPS
                if (isOPen(mContext)) {
                    toggleGPS(mContext, true);
                }
            }
            typeStr = getString(SportTaskUtil.getDetailTypeName(typeId,
                    typeDetailId));
        } else {
            typeStr = getString(SportTaskUtil.getTypeName(typeId));
            // 打开GPS
            if (isOPen(mContext)) {
                toggleGPS(mContext, true);
            }
        }
        sport_topTitle.setText(typeStr + "运动中");
        deviceId = sp.getInt("deviceId", 0);
        sporttype = new HashMap<String, String>();
        sporttype.put("type", typeStr);
        sporttype.put("save", "yes");

        if (SportsType.TYPE_CLIMBING == typeId) {
            pesulin.setVisibility(View.GONE);
            findViewById(R.id.info_line1).setVisibility(View.GONE);
            altitudeValue.setVisibility(View.VISIBLE);
            pressureValue.setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.info_line1).setVisibility(View.VISIBLE);
            pesulin.setVisibility(View.VISIBLE);
            altitudeValue.setVisibility(View.GONE);
            pressureValue.setVisibility(View.GONE);
        }

        peiSuList1 = new ArrayList<String>();
        peiSuList2 = new ArrayList<String>();
        peiSuList3 = new ArrayList<String>();
        peiSuList4 = new ArrayList<String>();
        speedList=new ArrayList<String>();
    }

    private void startSport() {
        MobclickAgent.onEventBegin(this, "uploadtask", sporttype.toString());
        startTimeSeconds = System.currentTimeMillis();

        Date startDate = new Date(startTimeSeconds);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        startTime = formatter.format(startDate);
        todayTime = formatter1.format(startDate);
        markCode = SportsApp.getInstance().getSportUser().getUid()
                + UUIDGenerator.getUUID();// 唯一标示

        task = new TimerTask() {
            public void run() {
                if (isStart) {
                    Message message = new Message();
                    message.what = UPDATE_STEP;
                    handler.sendMessage(message);
                }
            }
        };
        timer = new Timer(true);
        // 延时1000ms后执行，1000ms执行一次
        timer.schedule(task, 1000, 1000);
        // 设置每5秒获取一次GPS的定位信息
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 1, gpsLocationListener);
    }

    private int recLen = 0;
    private int mTempCount = 0;
    private Boolean isOpen = false;
    private Boolean lockisopen = false;
    private Boolean isStepBegin = false;
    private int playdis=0;//得到语音播报是按照公里
    private int playtime=0;//得到语音播报是按照时间
    private int playTempDis=0;//距离中间变量
    private int playTemptime=0;//时间中间变量

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_STEP:
                    recLen = (int) (mTempCount + (System.currentTimeMillis() - startTimeSeconds) / 1000L);
                    playTimeRound();//播报语音按照时间播报
                    if (!activityPause) {
                        chron.setText(SportTaskUtil.showTimeCount(recLen));
                        time_txt.setText(SportTaskUtil.showTimeCount(recLen));
                        if (pop_chronometerId != null) {
                            pop_chronometerId.setText(SportTaskUtil
                                    .showTimeCount(recLen));
                        }
                    }
                    if (recLen % (1 * 60) == 0) {
                        //根据规定的条件保存运动数据
                        if (typeId == SportsType.TYPE_CYCLING) {
                            if (dis * 1000 >= 500 && con > 0) {
                                // 骑行的时候小于500米不能保存
                                save(false);
                            }
                        } else {
                            if (dis * 1000 >= 100 && con > 0) {
                                // 其他方式的时候小于100米不能保存
                                save(false);
                            }
                        }


                    }

                    if (isStepBegin) {
                        updateStepInformation();
                    }
                    break;
                case UPDATE_LOCKSCREEN:
                    lockisopen = foxSportSetting.getBoolean("lockisopen", false);
                    if (!lockisopen) {
                        sliderLayout.setMainHandler(mmHandler);
                        lockscreen.setVisibility(View.VISIBLE);
                        startStopBut.setVisibility(View.INVISIBLE);
                        islock = true;
                        lockscreen.setOnTouchListener(new View.OnTouchListener() {

                            @Override
                            public boolean onTouch(View arg0, MotionEvent arg1) {
                                // TODO Auto-generated method stub
                                return true;
                            }
                        });
                        lockisopen = true;
                        Editor editor = foxSportSetting.edit();
                        editor.putBoolean("lockisopen", lockisopen);
                        editor.commit();
                    }
                    break;
                case GO_PAUSE:
                    vp = new VoicePrompt(getApplicationContext(), "female",
                            "loseGPS", null);
                    vp.playVoice();
                    goPause();
                    break;
                case YUYINBOBAONUM:
                    updateYuyinBobaoNum();
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private void addMediaItem(LatLng point, int typeID) {
        MarkerOptions marker = null;
        if (typeID == 1) {
            marker = new MarkerOptions().icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher)));
        } else if (typeID == 2) {
            marker = new MarkerOptions().icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher)));
        } else if (typeID == 3) {
            marker = new MarkerOptions().icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher)));
        }
        aMap.addMarker(marker.position(point));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bigBtn:
                if (islock){
                    if (isGone) {
                        unfoldView.setVisibility(View.VISIBLE);
                        unfoldBtn.setVisibility(View.VISIBLE);
                        unfoldBtn2.setVisibility(View.INVISIBLE);
                        sport_ll_lock.setVisibility(View.VISIBLE);
                        lockscreen.setVisibility(View.VISIBLE);
                        startStopBut.setVisibility(View.INVISIBLE);
                        isGone = false;
                    } else {
                        unfoldView.setVisibility(View.GONE);
                        unfoldBtn.setVisibility(View.INVISIBLE);
                        unfoldBtn2.setVisibility(View.VISIBLE);
                        sport_ll_lock.setVisibility(View.GONE);
                        lockscreen.setVisibility(View.GONE);
                        startStopBut.setVisibility(View.VISIBLE);
                        isGone = true;
                    }
                }else{
                    if (isGone) {
                        unfoldView.setVisibility(View.VISIBLE);
                        unfoldBtn.setVisibility(View.VISIBLE);
                        unfoldBtn2.setVisibility(View.INVISIBLE);
                        isGone = false;
                    } else {
                        unfoldView.setVisibility(View.GONE);
                        unfoldBtn.setVisibility(View.INVISIBLE);
                        unfoldBtn2.setVisibility(View.VISIBLE);
                        isGone = true;
                    }
                }
                break;

            case R.id.bigBtn2:
                if (islock){
                    if (isGone) {
                        unfoldView.setVisibility(View.VISIBLE);
                        unfoldBtn.setVisibility(View.VISIBLE);
                        unfoldBtn2.setVisibility(View.INVISIBLE);
                        sport_ll_lock.setVisibility(View.VISIBLE);
                        lockscreen.setVisibility(View.VISIBLE);
                        startStopBut.setVisibility(View.INVISIBLE);
                        isGone = false;
                    } else {
                        unfoldView.setVisibility(View.GONE);
                        unfoldBtn.setVisibility(View.INVISIBLE);
                        unfoldBtn2.setVisibility(View.VISIBLE);
                        sport_ll_lock.setVisibility(View.GONE);
                        lockscreen.setVisibility(View.GONE);
                        startStopBut.setVisibility(View.VISIBLE);
                        isGone = true;
                    }
                }else{
                    if (isGone) {
                        unfoldView.setVisibility(View.VISIBLE);
                        unfoldBtn.setVisibility(View.VISIBLE);
                        unfoldBtn2.setVisibility(View.INVISIBLE);
                        isGone = false;
                    } else {
                        unfoldView.setVisibility(View.GONE);
                        unfoldBtn.setVisibility(View.INVISIBLE);
                        unfoldBtn2.setVisibility(View.VISIBLE);
                        isGone = true;
                    }
                }
                break;
            case R.id.restartBtn:
                if (isPauseForGPS) {
                    Toast.makeText(mContext, getString(R.string.location_gps_weak),
                            Toast.LENGTH_SHORT).show();
                } else {
                    vp = new VoicePrompt(getApplicationContext(), "female",
                            "continue", null);
                    vp.playVoice();
                    goResume();
                }
                break;
            case R.id.stopBtn:
                if (canSave) {
                    goStop();
                } else {
                    Toast.makeText(mContext,
                            getString(R.string.spors_media_disable),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sporting_setting_btn:
                Intent  intent=new Intent(SportingMapActivityGaode.this,
                        SportstingSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.maptype_qiehuan:
                if(aMap!=null){
                    if(isMapQiehuan){
                        isMapQiehuan=false;
                        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                        findViewById(R.id.maptype_qiehuan).setBackgroundResource(R.drawable.maptype_qiehuan_unclick);
                    }else{
                        isMapQiehuan=true;
                        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                        findViewById(R.id.maptype_qiehuan).setBackgroundResource(R.drawable.mapdetailtype_qiehuan_click);
                    }
                }

                break;
            case R.id.sport_map_back:
                if (!lockisopen) {
                    if (canSave) {
                        canSave = false;
                        isStart = false;
                        Bundle bundle = new Bundle();
                        bundle.putString("title",
                                getString(R.string.confirm_exit_title));
                        bundle.putString("message",
                                getString(R.string.spors_confirm_stopandsave));
                        onCreateDialog(2, bundle, 0);
                        mPopMenuBack.setVisibility(View.VISIBLE);
                        RemoveLockScreenMSG();
                        if (timingMgr != null) {
                            timingMgr.cancleRepeatTimingFiveMinutes();
                        }

                    } else {
                        finish();
                    }
                }

                break;
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
//		if (isOpen) {
//			if (mWakeLock != null && mWakeLock.isHeld())
//				mWakeLock.release();
//		}
        RemoveLockScreenMSG();
        if (mUploadDialog != null && mUploadDialog.isShowing()) {
            mUploadDialog.dismiss();
        }
        super.onPause();
        MobclickAgent.onPageEnd("SportingMapActivityGaode");
        MobclickAgent.onPause(mContext);
    }

    @Override
    public void onResume() {
//        if (mWakeLock != null && !mWakeLock.isHeld()) {
//            mWakeLock.acquire();
//        }
        activityPause = false;
        updateYuyinBobaoNum();

        if (mMapView != null) {
            mMapView.onResume();
        }

        if(aMap!=null){
            if(isMapQiehuan){
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
            }else{
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            }
        }
        if (isStart) {
            mDrawTrajectoryHandler.removeMessages(DRAWTRAJECTORY);
            mDrawTrajectoryHandler.sendEmptyMessage(DRAWTRAJECTORY);
        }
        SendLockScreenMSG();

        if(mSportsApp!=null&&mSportsApp.getSportUser()!=null){
            if(mSportsApp.getSportUser().getUid()!=0){
                uid = mSportsApp.getSportUser().getUid();
            }
        }

        sp = mContext.getSharedPreferences("sport_state_" + uid, 0);
        if (sp != null) {
            typeId = sp.getInt("typeId", 1);
        }
        if (typeId == SportsType.TYPE_WALK || typeId == SportsType.TYPE_RUN
                || typeId == SportsType.TYPE_CLIMBING) {
        } else {
            imageview_jibu.setVisibility(View.GONE);
        }

        MobclickAgent.onPageStart("SportingMapActivityGaode");
        MobclickAgent.onResume(mContext);
        super.onResume();
    }


    @Override
    public void onDestroy() {
        sportHandler.removeCallbacksAndMessages(null);
        if (locationManager != null) {
            locationManager.removeUpdates(gpsLocationListener);
            locationManager.removeGpsStatusListener(listener);
        }
        if (timer != null) {
            timer.cancel();
        }
        if (timingMgr != null) {
            timingMgr.cancleRepeatTimingFiveMinutes();
        }
        if (aMap != null) {
            aMap.setLocationSource(null);
            aMap.setMyLocationEnabled(false);
            aMap.clear();
            aMap = null;
        }
        deactivate();
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }
        if (mGeoPoints != null) {
            mGeoPoints.clear();
            mGeoPoints = null;
        }
        if (mDrawPoints != null) {
            mDrawPoints.clear();
            mDrawPoints = null;
        }
        if (sporttype != null) {
            sporttype.clear();
            sporttype = null;
        }
        if (db != null) {
            db.close();
            db=null;
        }
        if (isRun) {
            stopService();
            isRun = false;
        }
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
        RemoveLockScreenMSG();
        unregisterReceiver(timeReceiver);

        if (screenListener != null) {
            screenListener.unregisterListener();
        }

        unregisterHomeKeyReceiver(this);
        if (typeId != SportsType.TYPE_CYCLING) {
            sensorManager.unregisterListener(this);
        }

        hashMap = null;
        peiSuList1=null;
        peiSuList2=null;
        peiSuList3=null;
        peiSuList4=null;

        mSportsApp.isRunning=false;
        list_bitmap_path_upload=null;
        if (speedList != null) {
            speedList.clear();
            speedList = null;
        }

        if (mUploadDialog != null && mUploadDialog.isShowing()) {
            mUploadDialog.dismiss();
        }
        if(sportsMarkDisList!=null){
            sportsMarkDisList.clear();
            sportsMarkDisList=null;
        }
        strSportsMarkDisList=null;
        strSpeedList=null;
        pointString=null;

        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    class AddCoinsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated
            ApiBack results = (ApiBack) msg.obj;
//            if (mLoadProgressDialog != null&&mLoadProgressDialog.isShowing()) {
//                mLoadProgressDialog.dismiss();
//            }
            if(results!=null&&findString!=null&&results.getReg()>0){
                //表示发表运动秀成功
                SendMsgDetail mSendMsgDetail = new SendMsgDetail();
                mSendMsgDetail.setMethod_str(findString);
                mSendMsgDetail.setTimes(System.currentTimeMillis());
                mSendMsgDetail.setFindId(results.getReg()+"");
                mSendMsgDetail.setComeFrom(cityname);
                if(mSportsApp!=null){
                    mSportsApp.setmSendMsgDetail(mSendMsgDetail);
                }
                Bimp.bmp.clear();
                Bimp.drr.clear();
                Bimp.max = 0;
                Looper.getMainLooper();
            }
            switch (msg.what) {
                case ApiConstant.COINS_SUCCESS:
                    int grow_now = Integer.parseInt(results.getMsg());
                    if (coins == grow_now) {
                        SportTaskUtil.jump2CoinsDialog(
                                SportingMapActivityGaode.this,
                                getString(R.string.sports_coins, grow_now));
                    } else {
                        SportTaskUtil.jump2CoinsDialog(
                                SportingMapActivityGaode.this,
                                getString(R.string.sports_coins_limit_sucess,
                                        grow_now));
                    }

                    break;
                case ApiConstant.COINS_LIMIT:
                    Toast.makeText(mContext,
                            getString(R.string.sports_coins_limit_fail),
                            Toast.LENGTH_SHORT).show();
                    break;
                case ApiConstant.COINS_FAIL:
                    break;
                default:
                    break;
            }
            Message msgs = Message.obtain();
            msgs.what = thereturn;
            msgs.obj = theApiBack;
            sportHandler.sendMessage(msgs);
        }
    }

    private void uploadcoins() {
        new AddCoinsThread(coins, 3, addCoinsHandler, typeId,1,findString).start();
    }

    private void updateJiBuStep(boolean add) {
        int saveResult = -1;
        String sportDate = startTime.substring(0, 10);
        int height = SportsApp.getInstance().getSportUser().getHeight();
        if (height <= 0) {
            height = 170;
        }
        int weight = SportsApp.getInstance().getSportUser().getWeight();
        if (weight <= 0) {
            weight = 65;
        }
        int steps = (int) ((dis * 100000) / (0.45 * height));
        double qianka = weight * dis * 1.036;

        BuShuTongJiDB jibuDB = BuShuTongJiDB.getInstance(mContext);

        Cursor cursor = jibuDB.query(uid, sportDate);

        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                stepsInDB = 0;

                ContentValues values = new ContentValues();
                values.put(BuShuTongJiDB.UID_I, uid);
                values.put(BuShuTongJiDB.ID_I, -1);
                if (add) {
                    values.put(BuShuTongJiDB.STEP_NUM_I, steps + stepsInDB);
                } else {
                    values.put(BuShuTongJiDB.STEP_NUM_I, stepsInDB);
                }
                values.put(BuShuTongJiDB.DISTANCE_D, dis);
                values.put(BuShuTongJiDB.STEP_CALORIE_D, qianka);
                values.put(BuShuTongJiDB.DAY_S, sportDate);
                values.put(BuShuTongJiDB.IS_UPLOAD_I, 0);

                saveResult = jibuDB.insert(values, false);
            } else {
                if (-1 == stepsInDB) {
                    stepsInDB = cursor.getInt(cursor.getColumnIndex(BuShuTongJiDB.STEP_NUM_I));
                }

                ContentValues values = new ContentValues();
                values.put(BuShuTongJiDB.UID_I, uid);
                values.put(BuShuTongJiDB.ID_I, -1);
                if (add) {
                    values.put(BuShuTongJiDB.STEP_NUM_I, steps + stepsInDB);
                } else {
                    values.put(BuShuTongJiDB.STEP_NUM_I, stepsInDB);
                }
                values.put(BuShuTongJiDB.DISTANCE_D, dis);
                values.put(BuShuTongJiDB.STEP_CALORIE_D, qianka);
                values.put(BuShuTongJiDB.DAY_S, sportDate);
                values.put(BuShuTongJiDB.IS_UPLOAD_I, 0);

                saveResult = jibuDB.update(values, uid, sportDate, false);
            }
        } else {
            stepsInDB = 0;

            ContentValues values = new ContentValues();
            values.put(BuShuTongJiDB.UID_I, uid);
            values.put(BuShuTongJiDB.ID_I, -1);
            if (add) {
                values.put(BuShuTongJiDB.STEP_NUM_I, steps + stepsInDB);
            } else {
                values.put(BuShuTongJiDB.STEP_NUM_I, stepsInDB);
            }
            values.put(BuShuTongJiDB.DISTANCE_D, dis);
            values.put(BuShuTongJiDB.STEP_CALORIE_D, qianka);
            values.put(BuShuTongJiDB.DAY_S, sportDate);
            values.put(BuShuTongJiDB.IS_UPLOAD_I, 0);

            saveResult = jibuDB.insert(values, false);
        }

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        if (jibuDB != null) {
            jibuDB.close();
            jibuDB = null;
        }


//        writeJiBuHistory("" + startTime + " SportingMapActivityGaode.java line 1447 新增 " + steps + " 步\n");
    }

//    private void writeJiBuHistory(String infos) {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            String path = Environment.getExternalStorageDirectory()
//                    .getAbsolutePath() + "/addJiBuHistory/";
//            File dir = new File(path);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            try {
//                FileOutputStream fos = new FileOutputStream(path + "jibuhistory.txt", true);
//                fos.write(infos.getBytes());
//                fos.close();
//            } catch (IOException e) {
//            }
//        }
//    }

    public Dialog onCreateDialog(int id, Bundle bundle, final int taskid) {
        String message = bundle.getString("message");
        switch (id) {
            case 1:
                Dialog loginPregressDialog = new Dialog(mContext,
                        R.style.sports_dialog);
                LayoutInflater mInflater = mContext.getLayoutInflater();
                View v1 = mInflater.inflate(R.layout.sports_progressdialog, null);
                TextView msg = (TextView) v1.findViewById(R.id.message);
                msg.setText(message);
                v1.setMinimumWidth((int) (SportsApp.ScreenWidth * 0.8));
                loginPregressDialog.setContentView(v1);
                loginPregressDialog.setCancelable(false);
                loginPregressDialog.setCanceledOnTouchOutside(false);
                loginPregressDialog.setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });
                return loginPregressDialog;
            case 2:
                alertDialog = new Dialog(SportingMapActivityGaode.this,
                        R.style.sports_dialog1);
                alertDialog.setCanceledOnTouchOutside(false);
                LayoutInflater mInflater1 = getLayoutInflater();
                View v = mInflater1
                        .inflate(R.layout.sportingmap_daka_layout, null);
                v.findViewById(R.id.daka1).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                endSports(1);
                                endSportType = 1;
                            }
                        });
                v.findViewById(R.id.daka2).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                endSportType = 2;
                                endSports(2);
                            }
                        });
                v.findViewById(R.id.daka3).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                endSportType = 3;
                                endSports(3);
                            }
                        });

                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                                && event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (alertDialog.isShowing())
                                alertDialog.dismiss();
                            mPopMenuBack.setVisibility(View.GONE);
                            isStart = true;
                            if (mWakeLock != null && !mWakeLock.isHeld())
                                mWakeLock.acquire();
                            SendLockScreenMSG();
                            if (timingMgr != null) {
                                timingMgr.repeatTimingFiveMinutes();
                            }
                            startTimeSeconds = System.currentTimeMillis();
                            mTempCount = recLen;
                            canSave = true;
                        }
                        return false;
                    }

                });
                v.setMinimumWidth((int) (SportsApp.ScreenWidth * 0.9));
                alertDialog.setCancelable(true);
                alertDialog.setContentView(v);
                alertDialog.show();
                break;
        }
        return null;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void getPeisu() {
        if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
            if(peiSuList1.size()==peiSuList2.size()&&peiSuList2.size()==peiSuList3.size()&&peiSuList3.size()==peiSuList4.size()){
                List<GetPeisu> list = new ArrayList<GetPeisu>();
                GetPeisu gp;
                for (int i = 0; i < peiSuList1.size(); i++) {
                    gp = new GetPeisu();
                    gp.setSport_distance(peiSuList1.get(i));
                    gp.setSprots_time(peiSuList3.get(i));
                    gp.setSprots_velocity(peiSuList2.get(i));
                    gp.setgPS_type(peiSuList4.get(i));
                    list.add(gp);
                }
                mPeiInfo.setListpeis(list);
            }
        }
    }

    private void uploadSportTask() {
        if (mGeoPoints != null) {
            pointString = SportTrajectoryUtilGaode
                    .listLatLngToString(mGeoPoints);
        } else {
            pointString = null;
        }
        if((speedList!=null&&speedList.size()>0)){
            strSpeedList = SportTrajectoryUtilGaode.peiListToString(speedList);
        }else {
            strSpeedList = "";
        }

        if((sportsMarkDisList!=null&&sportsMarkDisList.size()>0)){
            strSportsMarkDisList = SportTrajectoryUtilGaode.listSportsMarkDisToString(sportsMarkDisList);
        }else {
            strSportsMarkDisList = "";
        }
        avg_speed=(dis * 3600) / recLen;
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                ApiBack apiBack = null;
                try {
//                    apiBack = ApiJsonParser.uploadSportTask(limitId,
//                            mSportsApp.getSessionId(), typeId, typeDetailId,
//                            deviceId, startTime, recLen, dis, con, (dis * 3600)
//                                    / recLen, heart, pointString, stepNum,
//                            SportsApp.MAP_TYPE_GAODE,
//                            DeviceUuidFactory.getDeviceSerial(), markCode,(speedList!=null&&speedList.size()>0)?SportTrajectoryUtilGaode.peiListToString(speedList):"");
                    apiBack = ApiJsonParser.uploadSportsTwoInfo(limitId,
                            mSportsApp.getSessionId(), typeId, typeDetailId,
                            deviceId, startTime, recLen, dis, con, avg_speed, heart, pointString, stepNum,
                            SportsApp.MAP_TYPE_GAODE, DeviceUuidFactory.getDeviceSerial(), markCode,strSpeedList,strSportsMarkDisList,
                            "z" + getResources().getString(R.string.config_game_id), mPeiInfo,2);
                    if (apiBack != null && (apiBack.getFlag() == 1||apiBack.getFlag() == 1010)) {
                        isUpload = 1;
                        grouptype=apiBack.getGrouptype();
                        taskID = apiBack.getReg();
//                        ed.putInt("taskID", taskID);
//                        ed.commit();
                        theApiBack = apiBack;
                        thereturn = UPLOAD_START;
                        uploadcoins();
                        //发表
//                        if(apiBack.getFlag() == 1010){
//                            if (mSportsApp!=null&&mSportsApp.isOpenNetwork()) {
//                                if(typeId == SportsType.TYPE_CLIMBING){
//                                    if (mPeiInfo != null) {
//                                        //上传运动配速的线程
//                                        if(mSportsApp!=null){
//                                            new UploadPeisuThread(SportingMapActivityGaode.this,1).start();
//                                        }
//                                    }
//                                }else {
//                                    if(dis>=1){
//                                        if (mPeiInfo != null) {
//                                            //上传运动配速的线程
//                                            if(mSportsApp!=null){
//                                                new UploadPeisuThread(SportingMapActivityGaode.this,1).start();
//                                            }
//                                        }
//                                    }
//                                }
//
//                            }
//                        }

                    } else if(apiBack != null && (apiBack.getFlag() == -100||apiBack.getFlag() == -56)){
                        //wifi链接但是没有实际链接外网或者网络超时
                        isUpload = -1;
                        Message msgfail = Message.obtain();
                        msgfail.what = apiBack.getFlag();
                        msgfail.obj = apiBack;
                        sportHandler.sendMessage(msgfail);
                    } else {
                        isUpload = -1;
                        Message msgfail = Message.obtain();
                        msgfail.what = UPLOAD_START;
                        msgfail.obj = apiBack;
                        sportHandler.sendMessage(msgfail);
                    }
                } catch (ApiNetException e)
                {
                    e.printStackTrace();
                    isUpload = -1;
                    msg.what = UPLOAD_PARAM_ERROR;
                    sportHandler.sendMessage(msg);
                    return;
                } catch (ApiSessionOutException e)
                {
                    e.printStackTrace();
                    isUpload = -1;
                    Message msgfail = Message.obtain();
                    msgfail.what = UPLOAD_START;
                    msgfail.obj = apiBack;
                    sportHandler.sendMessage(msgfail);
                }
            }
        }.start();
    }

    private void updateSportTask() {
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                if (mGeoPoints != null) {
                    pointString = SportTrajectoryUtilGaode
                            .listLatLngToString(mGeoPoints);
                } else {
                    pointString = null;
                }
                ApiBack apiBack = null;
                try {
                    apiBack = ApiJsonParser
                            .updateSportTask(taskID, limitId,
                                    mSportsApp.getSessionId(), typeId,
                                    deviceId, startTime, recLen, dis, con,
                                    (dis * 3600) / recLen, heart, pointString,
                                    stepNum, mSportsApp.mCurMapType);
                    if (apiBack != null && apiBack.getFlag() == 1) {
                        isUpload = 1;
                        theApiBack = apiBack;
                        thereturn = UPLOAD_UPDATE;
                        uploadcoins();
                    } else {
                        isUpload = -1;
                        Message msgfail = Message.obtain();
                        msgfail.what = UPLOAD_UPDATE;
                        msgfail.obj = apiBack;
                        sportHandler.sendMessage(msgfail);
                    }
                } catch (ApiNetException e) {
                    e.printStackTrace();
                    msg.what = UPLOAD_PARAM_ERROR;
                    sportHandler.sendMessage(msg);
                    return;
                } catch (ApiSessionOutException e) {
                    e.printStackTrace();
                    isUpload = -1;
                    Message msgfail = Message.obtain();
                    msgfail.what = UPLOAD_UPDATE;
                    msgfail.obj = apiBack;
                    sportHandler.sendMessage(msgfail);
                }
            }
        }.start();
    }


    private void saveSportTask() {
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = SAVE_LOCAL;
                sportHandler.sendMessage(msg);
            }
        }.start();
    }

    private void deleteSportTaskByID() {
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                ApiBack apiBack = null;
                try {
                    apiBack = ApiJsonParser.deleteSportsTaskById(
                            mSportsApp.getSessionId(), uid, taskID);
                } catch (ApiNetException e) {
                    e.printStackTrace();
                } catch (ApiSessionOutException e) {
                    e.printStackTrace();
                }
                msg.what = UPLOAD_DELETE;
                msg.obj = apiBack;
                sportHandler.sendMessage(msg);
            }
        }.start();
    }

    private Handler sportHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_START:
                    ApiBack back = (ApiBack) msg.obj;
                    if (back != null && back.getFlag() == 1) {
                        if (isMedia == false) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.upload_success),
                                    Toast.LENGTH_SHORT).show();

                        }
                        saveDBPeisu(back);
                        // 上传成功更新用户数据信息
                        if (mSportsApp != null) {
                            if (mSportsApp.getmHandler() != null) {
                                mSportsApp.getmHandler().sendEmptyMessage(
                                        MyFirstSportFragment.REFRESH_USER);
                            }

                        }

                    } else {
                        if (back != null && back.getFlag() == 1010) {
                            if(typeId == SportsType.TYPE_CLIMBING){
                                saveClimb();
                            }
                            // 上传成功更新用户数据信息
                            if (mSportsApp != null) {
                                if (mSportsApp.getmHandler() != null) {
                                    mSportsApp.getmHandler().sendEmptyMessage(
                                            MyFirstSportFragment.REFRESH_USER);
                                }

                            }
                        }


                        if (isMedia == false) {
                            Editor editor = foxSportSetting.edit();
                            editor.putBoolean("isupload", false);
                            editor.commit();
                            if (mSportsApp.LoginOption) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.upload_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.sports_goto_login),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    if (!isMedia) {
                        int res = save(false);
                        if (res > 0) {
                            if (typeDetailId < 0) {
                                typeDetailId = 0;
                            }
                        }
                        uploadDataToQQ();
                    }

                    isUpload = -1;
                    if (mUploadDialog != null && mUploadDialog.isShowing()) {
                        mUploadDialog.dismiss();
                    }
                    break;
                case UPLOAD_UPDATE:
                    int res = save(false);
                    if (res > 0) {
                        if (typeDetailId < 0) {
                            typeDetailId = 0;
                        }
                        uploadDataToQQ();
                    }

                    ApiBack upback = (ApiBack) msg.obj;
                    if (upback != null && upback.getFlag() == 1) {
                        Toast.makeText(mContext,
                                getString(R.string.upload_success),
                                Toast.LENGTH_SHORT).show();
                        Log.i("上传数据", "上传数据---->" + upback.getMsg());
                    } else {

                        Editor editor = foxSportSetting.edit();
                        editor.putBoolean("isupload", false);
                        editor.commit();

                        if (mSportsApp.LoginOption) {
                            Toast.makeText(mContext,
                                    getString(R.string.upload_failed),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext,
                                    getString(R.string.sports_goto_login),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    isUpload = -1;
                    if (mUploadDialog != null && mUploadDialog.isShowing()) {
                        mUploadDialog.dismiss();
                    }
                    break;
                case UPLOAD_PARAM_ERROR:
                    if (!isMedia) {
                        int res1 = save(false);
                        if (res1 > 0) {
                            if (typeDetailId < 0) {
                                typeDetailId = 0;
                            }
                        }
                        // 即使提交失败也跳转
                        uploadDataToQQ();
                    }
                    if (mUploadDialog != null && mUploadDialog.isShowing()) {
                        mUploadDialog.dismiss();
                    }
                    canSave = true;
                    Toast.makeText(mContext, getString(R.string.upload_failed),
                            Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_DELETE:
                    ApiBack back3 = (ApiBack) msg.obj;
                    if (back3 != null && back3.getFlag() == 1) {
                        Log.i("UPLOAD_DELETE", "删除成功！" + back3.getMsg());
                    }
                    break;
                case SAVE_LOCAL:
                    int res1 = save(false);
                    if (res1 > 0) {
                        if (typeDetailId < 0) {
                            typeDetailId = 0;
                        }
                    }
                    gotoSportDetailActivity();
                    break;
                case -56:
                case -100:
                    Editor editor = foxSportSetting.edit();
                    editor.putBoolean("isupload", false);
                    editor.commit();
                    Editor qEditor = mContext.getSharedPreferences(
                            "qq_health_sprots", 0).edit();
                    qEditor.putString(startTime, startTime);
                    qEditor.commit();
                    Toast.makeText(
                            mContext,
                            getString(R.string.error_cannot_access_net),
                            Toast.LENGTH_SHORT).show();
                    isUpload = -1;
                    saveSportTask();
                    break;


            }
        }

        ;
    };

    private void uploadDataToQQ() {
        int sportGoal =0;
        if(foxSportSetting!=null){
            sportGoal= foxSportSetting.getInt("editDistance", 0);
        }
        boolean result = SportTaskUtil.send2QQ(mContext, typeId, sportGoal,
                startTime, startTimeSeconds / 1000L + "", uid,
                new QQHealthResult() {

                    @Override
                    public void qqResult() {
                        gotoSportDetailActivity();
                    }
                });// 运动数据上传至qq健康平台
        if (!result) {
            gotoSportDetailActivity();
        }
    }

    private void gotoSportDetailActivity() {
        Intent intent;
        if(grouptype!=null&&!"".equals(grouptype)&&!"null".equals(grouptype)){
            intent = new Intent(SportingMapActivityGaode.this,
                    SportsHonorActivity.class);
            intent.putExtra("fromPage", 1);
            intent.putExtra("grouptype", grouptype);
        }else{
            intent = new Intent(SportingMapActivityGaode.this,
                    SportTaskDetailActivityGaode.class);
        }
        intent.putExtra("taskid", taskID);
        intent.putExtra("uid", uid);
        intent.putExtra("startTime", startTime);
        intent.putExtra("mark_code", markCode);
        startActivity(intent);
        finish();
    }

    private Handler mDrawTrajectoryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DRAWTRAJECTORY:
                    new Thread() {
                        public void run() {
                            drawMap();
                        }

                        ;
                    }.start();
                    break;
            }
        }
    };

    private int cur_index = 0;

    private void drawMap() {
        int size = mDrawPoints.size();
        if (size > 0) {
            locData = mDrawPoints.get(size - 1);
        }
        if (mGeoPoints.size() == 1) {
            if (mMarkerStart == null) {
                mMarkerStart = aMap.addMarker(mMarkerOpStart.position(locData));
            } else {
                mMarkerStart.setPosition(locData);
            }
        }
//        else if(size == 1){
//                            MarkerOptions MarkerMiddle = new MarkerOptions()
//                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                                .decodeResource(getResources(),
//                                        R.drawable.map_middle)));
//                aMap.addMarker(MarkerMiddle.position(locData));
//        }
        else if (size > 1) {
            if (mMarkerMiddle == null) {
                mMarkerMiddle = aMap.addMarker(mMarkerOpMiddle
                        .position(locData));
            } else {
                mMarkerMiddle.setPosition(locData);
            }
            mMarkerMiddle.setPeriod(1);

            if (mPolyline == null) {
                options = new PolylineOptions().width(16).geodesic(true)
                        .color(Color.argb(255, 254,51,0));
                options.addAll(mDrawPoints);
                mPolyline = aMap.addPolyline(options);
                cur_index = size;
            } else {
                for (; cur_index < size; cur_index++) {
                    options.add(mDrawPoints.get(cur_index));
                }
                mPolyline.setPoints(options.getPoints());
            }
        }
        try {
            if (mMapView != null) {
                if (locData != null) {
                    animateTo(locData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private double disVal = 0;
    private int randomCoins;
    // private int mRandom = 2;
    private int mRandom = 5;
    private double mKilometre = 1;

    private boolean LocationUpdate(AMapLocation location) {
        int size = mGeoPoints.size();
        if (size == 0) {
            if (location == null)
                return false;
            mLat = location.getLatitude();
            mLng = location.getLongitude();
            mGeoPoints.add(new LatLng(mLat, mLng));
            mDrawPoints.add(new LatLng(mLat, mLng));
            speedList.add(location.getSpeed()+"");
            if (isStart)
                mDrawTrajectoryHandler.sendEmptyMessage(DRAWTRAJECTORY);
        } else if (size > 0) {
            if (location == null)
                return false;
            if (processResume) {
                processResume = false;
                mLat = location.getLatitude();
                mLng = location.getLongitude();
                mGeoPoints.add(new LatLng(mLat, mLng));
                mDrawPoints.add(new LatLng(mLat, mLng));
                speedList.add(location.getSpeed()+"");
                if (isStart)
                    mDrawTrajectoryHandler.sendEmptyMessage(DRAWTRAJECTORY);
                return true;
            }

            //自己写的两点之间的距离
//            double dmi = SportTaskUtil.gps2m(mLat, mLng,
//                    location.getLatitude(), location.getLongitude());

            LatLng startLat=new LatLng(mLat,mLng);
            LatLng endLat=new LatLng(location.getLatitude(),location.getLongitude());
            double dmi=AMapUtils.calculateLineDistance(startLat,endLat);//高德提供的两点之间的距离
            double mi =  Math.abs(dmi);
            startLat=null;
            endLat=null;


            if (locData.latitude != location.getLatitude()
                    || locData.longitude != location.getLongitude()) {
//				if ((mi > 0 && mi < ((typeId == SportsType.TYPE_CYCLING)?166:50))) {
                if ((mi > 0)) {
                    double lastmi = 0;
                    if (lastPointInValid) {
                        LatLng latlng = mGeoPoints.get(size - 1);
                        if (latlng.latitude != SportTrajectoryUtilGaode.INVALID_LATLNG
                                && latlng.longitude != SportTrajectoryUtilGaode.INVALID_LATLNG) {
                            lastmi = Math.abs(SportTaskUtil.gps2m(
                                    latlng.latitude, latlng.longitude, mLat,
                                    mLng));
                        }
                    }
                    disVal = disVal + mi * 1.0;
                    disVal = disVal + lastmi * 1.0;
                    Log.i("disVal", "disVal = " + disVal);
                    dis = (mi+lastmi) * 1.0 / 1000 + dis;
                    temDis = 0;
                    String disStringValue = SportTaskUtil.getDoubleNumber(dis);
                    time_disValue.setText(disStringValue);
                    zong_li_txt.setText(disStringValue);
                    double avgSpeed = (dis * 3600) / recLen;
                    Boolean normalRange = SportTaskUtil.getNormalRange(typeId,
                            avgSpeed, recLen);
//                    if (normalRange && !speedOverLimit && dis >= 1
//                            && Math.floor(dis) % mKilometre == 0) {
                    //去掉速度限制语音播报控制
                    playDisRound();//按照公里数播报语音
                    if (dis >= 1 && Math.floor(dis) % mKilometre == 0) {
                        sportsMarkDisList.add(new SportsMarkDis((int)dis,location.getLatitude(),location.getLongitude()));
                        mKilometre += 1;
                        String pace = String.valueOf(paceStringValue);

//                        hashMap.put("gongli", String.valueOf((int) (dis)));

//                        hashMap.put(
//                                "yongshi",
//                                String.valueOf(System.currentTimeMillis()
//                                        - startTimeSeconds));
//                        hashMap.put(
//                                "yongshi",
//                                String.valueOf(recLen*1000));

//						hashMap.put("peisu_fen",
//								pace.substring(0, pace.indexOf("'")));
//						hashMap.put(
//								"peisu_miao",
//								pace.substring(pace.indexOf("'") + 1,
//										pace.indexOf("\"")));
                        if (typeId == SportsType.TYPE_WALK
                                || typeId == SportsType.TYPE_RUN
                                || typeId == SportsType.TYPE_CYCLING) {
                            //只有跑步，骑行，走路有配速
                            int result = 0;
                            if (savePeisuFirst) {
                                String gl = String.valueOf((int) (dis));
                                if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                                    peiSuList1.add(gl);
                                    String tempPeisu=recLen * 1000 + "";
                                    peiSuList2.add(tempPeisu);
                                    peiSuList3.add(tempPeisu);
                                    peiSuList4.add(gps_type+"");
                                    String p1,p2,p3,p4;
                                    p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                                    p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                                    p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                                    p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                                    result = SavePeisu(p1, p2, p3,p4, markCode);
                                    if (result > 0) {
                                        savePeisuFirst = false;
                                    }
                                    p1=null;
                                    p2=null;
                                    p3=null;
                                    p4=null;
                                }
                                gl = null;

                            } else {
                                //数据更新方法
                                String gl = String.valueOf((int) (dis));
                                if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                                    peiSuList1.add(gl);
                                    String tempPeisu=recLen * 1000 + "";
                                    peiSuList2.add(tempPeisu);
                                    peiSuList3.add(tempPeisu);
                                    peiSuList4.add(gps_type+"");
                                    String p1,p2,p3,p4;
                                    p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                                    p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                                    p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                                    p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                                    result = updatePeisu(p1, p2, p3,p4, markCode, 0 + "");
                                    p1=null;
                                    p2=null;
                                    p3=null;
                                    p4=null;

                                }
                                gl = null;
                            }


                        }
//                        vp = new VoicePrompt(getApplicationContext(), "female",
//                                "moving", hashMap);
//                        vp.playVoice();
                    }
                    // 现在测试总公里改成2公里就发随机金币 正式版本要改回五公里
                    if (normalRange && !speedOverLimit && dis >= 5
                            && Math.floor(dis) % mRandom == 0) {
                        // mRandom += 2;
                        mRandom += 5;
                        int randomNum = (int) (Math.random() * 100) + 1;
                        if (randomNum >= 1 && randomNum <= 69)
                            randomCoins = 1;
                        else if (randomNum >= 70 && randomNum <= 79)
                            randomCoins = 2;
                        else if (randomNum >= 80 && randomNum <= 85)
                            randomCoins = 3;
                        else if (randomNum >= 86 && randomNum <= 90)
                            randomCoins = 4;
                        else if (randomNum >= 91 && randomNum <= 94)
                            randomCoins = 5;
                        else if (randomNum >= 95 && randomNum <= 98)
                            randomCoins = 7;
                        else if (randomNum >= 99 && randomNum <= 100)
                            randomCoins = 9;
                        if (mSportsApp.isOpenNetwork()
                                && mSportsApp.LoginOption) {

                            if ("".equals(foxSportSetting.getString(
                                    "Coins_todayTime", ""))
                                    || !todayTime.equals(foxSportSetting
                                    .getString("Coins_todayTime", ""))) {
                                // 一天只奖励一次随机奖励

                                new AddCoinsThread(randomCoins, 2,
                                        new Handler() {

                                            @Override
                                            public void handleMessage(
                                                    Message msg) {
                                                ApiBack apiBack = (ApiBack) msg.obj;
                                                if (apiBack != null
                                                        && apiBack.getFlag() == 0) {
                                                    SportTaskUtil
                                                            .jump2CoinsDialog(
                                                                    SportingMapActivityGaode.this,
                                                                    getString(
                                                                            R.string.random_coins,
                                                                            randomCoins));

                                                    Editor edit = foxSportSetting
                                                            .edit();
                                                    edit.putString(
                                                            "Coins_todayTime",
                                                            todayTime);
                                                    edit.commit();
                                                    randomCoins = 0;
                                                } else {
                                                    // 随机金币布累加
                                                    foxSportSetting
                                                            .edit()
                                                            .putInt("randomCoins",
                                                                    randomCoins)
                                                            .commit();
                                                }
                                            }

                                        }, -1).start();

                            }

                        } else {
                            // 随机金币布累加
                            foxSportSetting.edit()
                                    .putInt("randomCoins", randomCoins)
                                    .commit();
                        }
                    }
                    if (typeId == SportsType.TYPE_WALK) {
                        con = (int) WalkingUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_RUN) {
                        con = (int) RunningUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_CLIMBING) {
                        con = (int) MountainUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_GOLF) {
                        con = (int) GolfingUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_WALKRACE) {
                        con = (int) WalkingRaceUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_CYCLING) {
                        con = (int) RidingUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_FOOTBALL) {
                        con = (int) FootballUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_ROWING) {
                        con = (int) RowingUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_SWIM) {
                        con = (int) SwimmingUtils.getCalories(dis, recLen,
                                typeDetailId + 1);
                    } else if (typeId == SportsType.TYPE_SKATING) {
                        con = (int) SkatingUtils.getCalories(dis);
                    } else if (typeId == SportsType.TYPE_ROLLERSKATING) {
                        con = (int) RollerSportsUtils.getCalories(dis);
                    }
                    conStringValue = con + "";
                    sporting_sportxiaohao.setText(conStringValue + "Cal");
                    if (lastPointInValid) {
                        mGeoPoints.add(new LatLng(mLat, mLng));
                        mDrawPoints.add(new LatLng(mLat, mLng));
                        speedList.add(location.getSpeed()+"");
                        lastPointInValid = false;
                    }
                    mLat = location.getLatitude();
                    mLng = location.getLongitude();
                    mGeoPoints.add(new LatLng(mLat, mLng));
                    mDrawPoints.add(new LatLng(mLat, mLng));
                    speedList.add(location.getSpeed()+"");
                    if (!activityPause && isStart)
                        mDrawTrajectoryHandler.sendEmptyMessage(DRAWTRAJECTORY);
                } else {
                    temDis=0;
                    mLat = location.getLatitude();
                    mLng = location.getLongitude();
                    lastPointInValid = true;
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        if (gps) {
            return true;
        }
        return false;
    }

    /**
     * GPS开关管理
     *
     * @param context
     */
    public void toggleGPS(Context context, Boolean isOn) {
        if (isOPen(context) == true) {
            String provider = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (!provider.contains("gps")) { // if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                mContext.sendBroadcast(poke);
            }
            Log.i("toggleGPS", "GPS已开启!");
        } else {
            Log.i("toggleGPS", "GPS未开启!");
        }
    }

    //配速存本地
    private int SavePeisu(String gongli, String peisu, String time, String gps_type,String bs) {
        int savainsert = 0;
        if (peisuDB == null) {
            peisuDB = PeisuDB.getmInstance(getApplicationContext());
        }
        ContentValues c = new ContentValues();
        c.put(PeisuDB.GongLi, gongli);
        c.put(PeisuDB.Peisu, peisu);
        c.put(PeisuDB.Time, time);
        c.put(PeisuDB.GPS_TYPE,gps_type);
        c.put(PeisuDB.SPORT_MARKCODE, bs);
        c.put(PeisuDB.SPORT_ISUPLOAD, 0 + "");
        savainsert = peisuDB.insert(c);

        return savainsert;
    }

    //配速更新本地
    private int updatePeisu(String gongli, String peisu, String time,String gps_type, String bs, String isupload) {
        int savainsert = 0;
        if (peisuDB == null) {
            peisuDB = PeisuDB.getmInstance(getApplicationContext());
        }
        ContentValues c = new ContentValues();
        c.put(PeisuDB.GongLi, gongli);
        c.put(PeisuDB.Peisu, peisu);
        c.put(PeisuDB.Time, time);
        c.put(PeisuDB.GPS_TYPE,gps_type);
        c.put(PeisuDB.SPORT_MARKCODE, bs);
        c.put(PeisuDB.SPORT_ISUPLOAD, isupload);
        savainsert = peisuDB.update(c, markCode, false);
        return savainsert;
    }

    //配速更新上传是否成功
    private int updateisSavalocal(String bs, String isupload) {
        int savainsert = 0;
        if (peisuDB == null) {
            peisuDB = PeisuDB.getmInstance(getApplicationContext());
        }
        ContentValues c = new ContentValues();
        c.put(PeisuDB.SPORT_MARKCODE, bs);
        c.put(PeisuDB.SPORT_ISUPLOAD, isupload);
        savainsert = peisuDB.update(c, markCode, false);
        return savainsert;
    }

    // 保存本地数据库
    private int save(boolean isShow) {
        // 地图定位点
        if (mGeoPoints != null) {
            pointString = SportTrajectoryUtilGaode
                    .listLatLngToString(mGeoPoints);
        } else {
            pointString = null;
        }
        if((speedList!=null&&speedList.size()>0)){
            strSpeedList = SportTrajectoryUtilGaode.peiListToString(speedList);
        }else {
            strSpeedList = "";
        }

        if((sportsMarkDisList!=null&&sportsMarkDisList.size()>0)){
            strSportsMarkDisList = SportTrajectoryUtilGaode.listSportsMarkDisToString(sportsMarkDisList);
        }else {
            strSportsMarkDisList = "";
        }
        if (typeId == SportsType.TYPE_WALK || typeId == SportsType.TYPE_RUN
                || typeId == SportsType.TYPE_CLIMBING) {

            stepNum = (int) (dis * 10000 / 7);
        } else {
            stepNum = 0;
        }

        // 开始运动时间(年月日)
        Date startDate = new Date(startTimeSeconds);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String sportDate = formatter.format(startDate);

        db = SportSubTaskDB.getInstance(getApplicationContext());
        ContentValues values = new ContentValues();
        values.put(SportSubTaskDB.UID, uid);
        values.put(SportSubTaskDB.LIMIT, limitId);
        values.put(SportSubTaskDB.SPORT_TYPE, typeId);
        values.put(SportSubTaskDB.SPORT_SWIM_TYPE, typeDetailId);
        values.put(SportSubTaskDB.SPORT_DEVICE, deviceId);
        values.put(SportSubTaskDB.SPORT_START_TIME, startTime);// startTime等同于startTimeSeconds
        values.put(SportSubTaskDB.SPORT_TIME, recLen);
        values.put(SportSubTaskDB.SPORT_STEP, stepNum);
        values.put(SportSubTaskDB.SPORT_DISTANCE, dis);
        values.put(SportSubTaskDB.SPORT_CALORIES, con);
        values.put(SportSubTaskDB.SPORT_SPEED, (dis * 3600) / recLen);
        values.put(SportSubTaskDB.SPORT_HEART_RATE, heart);
        values.put(SportSubTaskDB.SPORT_ISUPLOAD, isUpload);
        values.put(SportSubTaskDB.SPORT_DATE, sportDate);
        values.put(SportSubTaskDB.SPORT_TASKID, taskID);
        values.put(SportSubTaskDB.SPORT_LAT_LNG, pointString);
        values.put(SportSubTaskDB.SPORT_MAPTYPE, mSportsApp.mCurMapType);
        values.put(SportSubTaskDB.SPORT_MARKCODE, markCode);// 新增唯一标识码
        values.put(SportSubTaskDB.SPORT_SPEEDLIST,strSpeedList);
        values.put(SportSubTaskDB.SPORT_MARKLIST,strSportsMarkDisList);

        int result = 0;
        if (isFirstSave) {
            result = db.insert(values, isShow);
            if (result > 0) {
                isFirstSave = false;
            }
        } else {
            result = db.update(values, uid, startTime, false, markCode);
        }

        if (typeId == SportsType.TYPE_WALK || typeId == SportsType.TYPE_RUN
                || typeId == SportsType.TYPE_CLIMBING) {
            updateJiBuStep(true);
        }
        startDate=null;
        formatter=null;
        sportDate=null;
        return result;
    }

    private void startService() {
        startService(new Intent(SERVICE_NAME));
    }

    private void stopService() {
        stopService(new Intent(SERVICE_NAME));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCKSCREEN && resultCode == RESULT_OK) {
            return;
        }
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, getString(R.string.sd_card_is_invalid),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    private void initGPS() {
        gpsLocationListener = new GpsLocationListener();
        // 获取系统LocationManager服务
        locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(listener);
    }

    private double CalcPressure(double altitude) {
        BigDecimal bd1 = new BigDecimal(Double.toString(44330.0));
        BigDecimal bd2 = new BigDecimal(Double.toString(altitude));
        BigDecimal bd3 = new BigDecimal(Double.toString(44330.0));
        MathContext mc = new MathContext(4, RoundingMode.HALF_UP);
        return Math.pow(bd1.subtract(bd2).divide(bd3, mc).doubleValue(),
                100 / 19) * 101.28;
    }

    private void updateSpeed(Location location) {
        if (activityPause)
            return;
        // String altiStr = "--";
        if (location != null) {
            speed = location.getSpeed() * 3.6f;// 速度
            if (speed != 0) {
                pace = 3600 / speed;// 配速：单位 秒/公里
                otherSpeed = speed;
            } else {
                if (otherSpeed != 0) {
                    pace = 3600 / otherSpeed;// 配速：单位 秒/公里
                } else {
                    pace = 0;
                }
            }
        } else {
            speed = 0;
            pace = 0;
        }
        lastSpeed = newSpeed;
        newSpeed = speed;
        if (!SportTaskUtil.getNormalRange(typeId, newSpeed, recLen)) {
            if (!SportTaskUtil.getNormalRange(typeId, lastSpeed, recLen)) {
                maxSpeedNum++;
            } else {
                maxSpeedNum = 1;
            }
        } else {
            maxSpeedNum = 0;
        }
        if (maxSpeedNum > SPEEDLIMITNUM) {
            speedOverLimit = true;
        }
        speedStringValue = SportTaskUtil.getDoubleOneNum(speed);
        if(recLen>0){
            String avgSpeed=SportTaskUtil.getDoubleOneNum(dis*3600/(recLen));
            sporting_sport_pingjunSpeed.setText(avgSpeed + "km/h");
        }
        paceStringValue = (int) (pace / 60) + "'" + (int) (pace % 60) + "\"";
        sporting_sport_peiPace.setText(paceStringValue + "/km");
    }

    private class GpsLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            mCountNum = 0;
            lastCountNum = 10;
            updateSpeed(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (provider != null && !"".equals(provider)) {
                if (locationManager != null) {
                    updateSpeed(locationManager.getLastKnownLocation(provider));
                }
            }
        }

        @Override
        public void onStatusChanged(String arg0, int status, Bundle arg2) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    // Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    //  Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    // Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (!lockisopen) {
                    if (canSave) {
                        canSave = false;
                        isStart = false;
                        Bundle bundle = new Bundle();
                        bundle.putString("title",
                                getString(R.string.confirm_exit_title));
                        bundle.putString("message",
                                getString(R.string.spors_confirm_stopandsave));
                        onCreateDialog(2, bundle, 0);
                        mPopMenuBack.setVisibility(View.VISIBLE);
                        RemoveLockScreenMSG();
                        if (timingMgr != null) {
                            timingMgr.cancleRepeatTimingFiveMinutes();
                        }

                    } else {
                        finish();
                    }
                    return true;
                } else {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                RemoveLockScreenMSG();
                break;
            case MotionEvent.ACTION_UP:
                SendLockScreenMSG();
                break;

        }
        return super.onTouchEvent(event);
    }

    public void RemoveLockScreenMSG() {
        if (handler != null) {
            handler.removeMessages(UPDATE_LOCKSCREEN);
        }
    }

    // 自动锁定锁定
    public void SendLockScreenMSG() {
        if (isStart && handler != null && !lockisopen) {
            handler.sendEmptyMessageDelayed(UPDATE_LOCKSCREEN, LOCKSCREEN_DELAY);
        }
    }

    // 点击锁定按钮锁定
    public void SendBtnLockScreenMSG() {
        if (isStart && handler != null && !lockisopen) {
            handler.sendEmptyMessageDelayed(UPDATE_LOCKSCREEN, 0);
        }
    }

    private Handler mmHandler = new Handler() {

        public void handleMessage(Message msg) {
            if(msg==null){
                return;
            }
            if (MSG_LOCK_SUCESS == msg.what) {

                WindowManager.LayoutParams attr = getWindow().getAttributes();
                attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().setAttributes(attr);
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                foxSportSetting = getSharedPreferences("sports"
                        + mSportsApp.getSportUser().getUid(), 0);
                lockisopen = false;
                Editor editor = foxSportSetting.edit();
                editor.putBoolean("lockisopen", false);
                editor.commit();
                SendLockScreenMSG();
                lockscreen.setVisibility(View.GONE);// 锁屏成功时，结束我们的Activity界面
                startStopBut.setVisibility(View.VISIBLE);
                islock = false;

                vp = new VoicePrompt(getApplicationContext(), "female",
                        "pause", null);
                vp.playVoice();
                goPause();
            }
        }
    };

    @Override
    public void activate(OnLocationChangedListener arg0) {
//        if (mAMapLocationManager == null) {
        //           mAMapLocationManager = LocationManagerProxy.getInstance(this);
//            mAMapLocationManager.setGpsEnable(true);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
//            mAMapLocationManager.requestLocationData(
//                    LocationManagerProxy.GPS_PROVIDER, 5000, 1, this);
//              LocationProviderProxy.AMapNetwork;
//        }
        if (mapLocationClient == null) {
            mapLocationClient = new AMapLocationClient(this);
            mapLocationClientOption = new AMapLocationClientOption();
            // 设置定位监听
            mapLocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mapLocationClientOption
                    .setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            // 设置定位参数
            mapLocationClient.setLocationOption(mapLocationClientOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            // 设置定位间隔,单位毫秒,默认为2000ms
            mapLocationClientOption.setInterval(5000);
            mapLocationClient.startLocation();
        }

    }

    @Override
    public void deactivate() {
        if (mapLocationClient != null) {
            mapLocationClient.stopLocation();
            mapLocationClient.onDestroy();
        }
        mapLocationClient = null;
    }

    private int netError = 0;

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mLoadDialog != null && mLoadDialog.isShowing()) {
            mLoadDialog.dismiss();
        }
        if (amapLocation == null) {
            gps_type=2;
            climbHeight = 0;
            return;
        }

        tempAltitude = SportTaskUtil.getDoubleNum(amapLocation.getAltitude());

        altitudeValue.setText("高度(m)\n"
                + tempAltitude.substring(0, tempAltitude.indexOf('.')));
        pressureValue.setText("气压(kPa)\n"
                + SportTaskUtil.getDoubleNumber(CalcPressure(amapLocation
                .getAltitude())));
        if (dis * 1000 >= 10 ) {
            if (typeId == SportsType.TYPE_CLIMBING) {
                //表示登山的时候高度存本地
                if (dis > tempDis+0.02) {
                    //表示不是原地
                    saveClimb();
                }

            }
        }


//        String latitude = String.valueOf(amapLocation.getLatitude());
//        String longitude = String.valueOf(amapLocation.getLongitude());
//        Log.i("location", "这里" + latitude + "---" + longitude);

        if (String.valueOf(amapLocation.getLatitude()) == null || String.valueOf(amapLocation.getLongitude()) == null || String.valueOf(amapLocation.getLatitude()).equals("")
                || String.valueOf(amapLocation.getLatitude()).equals("0.0") || String.valueOf(amapLocation.getLongitude()).equals("")
                || String.valueOf(amapLocation.getLongitude()).equals("0.0") || String.valueOf(amapLocation.getLatitude()).equals("4.9E-324")
                || String.valueOf(amapLocation.getLongitude()).equals("4.9E-324")) {
            //记录下没有gps状态下的时间
            if(netError==0){
                difTime= System.currentTimeMillis();
            }

            if(difTime==0){
                difTime= System.currentTimeMillis();
            }else{
                //表示连续十五秒以上没有gps记录下状态
                if(((System.currentTimeMillis()-difTime)/1000)>=20){
                    stepPause();
                }
            }

            if (netError == 1) {
//                Toast.makeText(mContext, getString(R.string.location_fail),
//                        Toast.LENGTH_SHORT).show();
            }
            netError++;
            gps_type=2;
            return;
        }
        climbHeight = Integer.parseInt(tempAltitude.substring(0, tempAltitude.indexOf('.')));
        gps_type=1;
        netError=0;
        difTime=0;
        // 测试 距离减少原因
        mCountNum = 0;
        if(mGeoPoints!=null&&mGeoPoints.size()>0){
            if(amapLocation.getSpeed()>=0&&amapLocation.getSpeed()<=0.2){
                return;
            }
        }

        curLocData = new LatLng(amapLocation.getLatitude(),
                amapLocation.getLongitude());
        if (!mSportsApp.isStartY) {
            locData = new LatLng(amapLocation.getLatitude(),
                    amapLocation.getLongitude());
            animateTo(locData);
            if (mMarkerStart == null) {
                mMarkerStart = aMap.addMarker(mMarkerOpStart.position(locData));
            } else {
                mMarkerStart.setPosition(locData);
            }
        } else if (isStart) {
            if (LocationUpdate(amapLocation)) {
                locData = new LatLng(amapLocation.getLatitude(),
                        amapLocation.getLongitude());
            }
        }
    }

    private void animateTo(LatLng point) {
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoomValue),
                null);
    }


    BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    "com.fox.exercise.pedometer.TimerReceiver.alarmclock")) {
            }
        }
    };

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        if (arg0.getPosition() == 0 && sportype != 1) {
            if (canSave) {
                goStop();
            } else {
                Toast.makeText(mContext,
                        getString(R.string.spors_media_disable),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        Log.i("", "lulu arg0 getpostion2" + arg0.getPosition() + "--"
                + sportype);
        if (arg0.getPosition() == 0) {

        } else if (arg0.getPosition() == 1 && sportype == 2) {
            if (isPauseForGPS) {
                Toast.makeText(mContext, getString(R.string.location_gps_weak),
                        Toast.LENGTH_SHORT).show();
            } else {
                sportype = 3;
                vp = new VoicePrompt(getApplicationContext(), "female",
                        "pause", null);
                vp.playVoice();
            }
        } else if (arg0.getPosition() == 1 && sportype == 1) {
            if (is_start_map) {
                if ((locData != null && gpsType > 0) || (null != ss)) {
                    goStart();
                    sportype = 2;
                    is_start_map = false;
                } else {
                    getActionBar().removeAllTabs();
                    ActionBar bar = getActionBar();
                    bar.addTab(bar.newTab().setIcon(R.drawable.sk_stop)
                            .setTabListener(this));
                    bar.addTab(bar.newTab()
                            .setIcon(R.drawable.sk_start_or_stop)
                            .setTabListener(this));
                    if (gpsType > 0) {
                        Toast.makeText(mContext,
                                getString(R.string.in_gps_locating),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,
                                getString(R.string.location_gps_weak),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (arg0.getPosition() == 1 && sportype == 3) {
            if (isPauseForGPS) {
                Toast.makeText(mContext, getString(R.string.location_gps_weak),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    ;

    private void goStart() {
        if (findMethod) {
            getActionBar().removeAllTabs();
            ActionBar bar = getActionBar();
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_stop)
                    .setTabListener(this));
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_start)
                    .setTabListener(this));
        } else {
            stopBtn.setEnabled(true);
            restartBtn.setVisibility(View.VISIBLE);
        }
        preTime = FunctionStatic.onResume();
        if (typeId != SportsType.TYPE_CYCLING) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }
        isStart = true;
        startSport();
        startService();
        isRun = true;
        mSportsApp.isStartY = true;
        canSave = true;

        if (mWakeLock != null && !mWakeLock.isHeld())
            mWakeLock.acquire();
        if (handler != null && !lockisopen) {
            handler.sendEmptyMessage(UPDATE_LOCKSCREEN);
        }
        if (timingMgr != null) {
            timingMgr.repeatTimingFiveMinutes();
        }
    }

    private void goStop() {
        canSave = false;
        isStart = false;
        Bundle bundle = new Bundle();
        bundle.putString("title", getString(R.string.confirm_exit_title));
        bundle.putString("message",
                getString(R.string.spors_confirm_stopandsave));
        onCreateDialog(2, bundle, 0);
        mPopMenuBack.setVisibility(View.VISIBLE);
        RemoveLockScreenMSG();
        if (timingMgr != null) {
            timingMgr.cancleRepeatTimingFiveMinutes();
        }
    }

    private void goResume() {
        if (findMethod) {
            getActionBar().removeAllTabs();
            ActionBar bar = getActionBar();
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_stop)
                    .setTabListener(this));
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_start)
                    .setTabListener(this));
        }
        isStart = true;
        if (typeId != SportsType.TYPE_CYCLING) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }


//        if (mWakeLock != null && !mWakeLock.isHeld())
//            mWakeLock.acquire();
        SendBtnLockScreenMSG();
        if (timingMgr != null) {
            timingMgr.repeatTimingFiveMinutes();
        }
        startTimeSeconds = System.currentTimeMillis();
        mTempCount = recLen;
    }

    private void goPause() {
        if (findMethod) {
            getActionBar().removeAllTabs();
            ActionBar bar = getActionBar();
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_stop)
                    .setTabListener(this));
            bar.addTab(bar.newTab().setIcon(R.drawable.sk_start_or_stop)
                    .setTabListener(this));
        }
        if (typeId != SportsType.TYPE_CYCLING) {
            sensorManager.unregisterListener(this);
        }
        isStart = false;

//        if (mWakeLock != null && mWakeLock.isHeld())
//            mWakeLock.release();
        RemoveLockScreenMSG();
        if (timingMgr != null) {
            timingMgr.cancleRepeatTimingFiveMinutes();
        }
        processPause();
    }

    private boolean processResume = false;//表示刚刚进入了暂停状态

    private void processPause() {
        int size = mGeoPoints.size();
        if (size > 0) {
            // 起始点暂停，清除起始点记录，重新记录
            if (size == 1) {
                mGeoPoints.clear();
                mDrawPoints.clear();
                if(speedList!=null){
                    speedList.clear();
                }
                return;
            }
            // 上一次暂停后没有运动轨迹，再暂停时不记录状态
            if (mGeoPoints.get(size - 1).latitude == SportTrajectoryUtilGaode.INVALID_LATLNG
                    || mGeoPoints.get(size - 1).longitude == SportTrajectoryUtilGaode.INVALID_LATLNG) {
                return;
            }
            // 继续运动后只获取到一个点，又进入暂停状态，把这个点清除
            else if (mGeoPoints.get(size - 2).latitude == SportTrajectoryUtilGaode.INVALID_LATLNG
                    || mGeoPoints.get(size - 2).longitude == SportTrajectoryUtilGaode.INVALID_LATLNG) {
                mGeoPoints.remove(size - 1);
                if(speedList!=null&&speedList.size()==size){
                    speedList.remove(size - 1);
                }
                mDrawPoints.clear();
                processResume = true;
                return;
            }

            mGeoPoints.add(new LatLng(SportTrajectoryUtilGaode.INVALID_LATLNG,
                    SportTrajectoryUtilGaode.INVALID_LATLNG));
            speedList.add("0");
//            MarkerOptions mMarkerMiddle = new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                            .decodeResource(getResources(),
//                                    R.drawable.map_middle)));
//            aMap.addMarker(mMarkerMiddle.position(mDrawPoints.get(mDrawPoints
//                    .size() - 1)));
            if (mPolyline != null) {
                PolylineOptions lineOptions = new PolylineOptions().width(16)
                        .geodesic(true).color(Color.argb(255, 254,51,0));
                lineOptions.addAll(mDrawPoints);
                aMap.addPolyline(lineOptions);
                mPolyline.remove();
                mPolyline = null;
                options = null;
                cur_index = 0;
            }
            mDrawPoints.clear();
        }
    }

    /**
     * 切换到从gps切换到计步模式
     */
    private void stepPause() {
        int size=0;
        if(mGeoPoints!=null){
            size = mGeoPoints.size();
        }
        if (size > 0) {
            // 起始点暂停，清除起始点记录，重新记录
            if (size == 1) {
                mGeoPoints.clear();
                mDrawPoints.clear();
                if(speedList!=null){
                    speedList.clear();
                }
                return;
            }
            // 上一次暂停后没有运动轨迹，再暂停时不记录状态
            if (mGeoPoints.get(size - 1).latitude == SportTrajectoryUtilGaode.INVALID_LATLNG
                    || mGeoPoints.get(size - 1).longitude == SportTrajectoryUtilGaode.INVALID_LATLNG) {
                mDrawPoints.clear();
                processResume = true;
                return;
            }
            // 继续运动后只获取到一个点，又进入暂停状态，把这个点清除
            else if (mGeoPoints.get(size - 2).latitude == SportTrajectoryUtilGaode.INVALID_LATLNG
                    || mGeoPoints.get(size - 2).longitude == SportTrajectoryUtilGaode.INVALID_LATLNG) {
                mGeoPoints.remove(size - 1);
                if(speedList!=null&&speedList.size()==size){
                    speedList.remove(size - 1);
                }
                mDrawPoints.clear();
                processResume = true;
                return;
            }

            mGeoPoints.add(new LatLng(SportTrajectoryUtilGaode.INVALID_LATLNG,
                    SportTrajectoryUtilGaode.INVALID_LATLNG));
            speedList.add("0");
            if (mPolyline != null) {
                PolylineOptions lineOptions = new PolylineOptions().width(16)
                        .geodesic(true).color(Color.argb(255, 254,51,0));
                lineOptions.addAll(mDrawPoints);
                aMap.addPolyline(lineOptions);
                mPolyline.remove();
                mPolyline = null;
                options = null;
                cur_index = 0;
            }
            mDrawPoints.clear();
        }
    }

    private int gpsType = 0;
    private int gpsTypeAdd, gpsTypeOtherAdd = 0;
    private long difTime=0;//判断gps状态切换的时间

    // 状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前GPS信号强度状态
                    getGPSStatus();

                    if (gpsType > 0) {
                        gpsTypeAdd = 0;
                        difTime=0;
                        if (gpsTypeOtherAdd == 0) {
                            TextView textView = (TextView) LayoutInflater.from(
                                    mContext).inflate(R.layout.toast_layout, null);
                            textView.setText("GPS模式记录中");
                            Toast mToast = new Toast(mContext);
                            mToast.setView(textView);
                            mToast.setDuration(Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.TOP, 30, 180);
                            mToast.show();
                        }
                        gpsTypeOtherAdd++;

                    } else {
                        gpsTypeOtherAdd = 0;
                        if (gpsTypeAdd == 0) {
                            if (typeId == SportsType.TYPE_WALK
                                    || typeId == SportsType.TYPE_RUN
                                    || typeId == SportsType.TYPE_CLIMBING) {
                                TextView textView = (TextView) LayoutInflater.from(
                                        mContext).inflate(R.layout.toast_layout,
                                        null);
                                textView.setText("GPS信号弱，转换为计步模式");
                                Toast mToast = new Toast(mContext);
                                mToast.setView(textView);
                                mToast.setDuration(Toast.LENGTH_SHORT);
                                mToast.setGravity(Gravity.TOP, 30, 180);
                                mToast.show();
                            }else{
                                TextView textView = (TextView) LayoutInflater.from(
                                        mContext).inflate(R.layout.toast_layout,
                                        null);
                                textView.setText("GPS信号弱,请到开阔地带");
                                Toast mToast = new Toast(mContext);
                                mToast.setView(textView);
                                mToast.setDuration(Toast.LENGTH_SHORT);
                                mToast.setGravity(Gravity.TOP, 30, 180);
                                mToast.show();
                            }

                            //记录下没有gps状态下的时间
                            difTime= System.currentTimeMillis();

                        }
                        gpsTypeAdd++;
                        if(difTime==0){
                            difTime= System.currentTimeMillis();
                        }else{
                            //表示连续十五秒以上没有gps记录下状态
                            if(((System.currentTimeMillis()-difTime)/1000)>20){
                                gps_type=2;
                                stepPause();
                            }
                        }

                    }

                    if (gpsType <= 0) {
                        // imageGps.setImageResource(R.drawable.sportmap_gps_0);
                        imageGps.setVisibility(View.GONE);
                        if (typeId == SportsType.TYPE_WALK
                                || typeId == SportsType.TYPE_RUN
                                || typeId == SportsType.TYPE_CLIMBING) {
                            imageview_jibu.setVisibility(View.VISIBLE);
                        } else {
                            imageview_jibu.setVisibility(View.GONE);
                            imageGps.setVisibility(View.VISIBLE);
                            imageGps.setImageResource(R.drawable.gps_g00);
                        }

                    } else if (gpsType == 1) {
                        imageGps.setVisibility(View.VISIBLE);
                        imageview_jibu.setVisibility(View.GONE);
                        // imageGps.setImageResource(R.drawable.sportmap_gps_1);
                        imageGps.setImageResource(R.drawable.gps_g01);
                    } else if (gpsType == 2) {
                        imageGps.setVisibility(View.VISIBLE);
                        imageview_jibu.setVisibility(View.GONE);
                        // imageGps.setImageResource(R.drawable.sportmap_gps_2);
                        imageGps.setImageResource(R.drawable.gps_g02);
                    } else if (gpsType == 3) {
                        imageGps.setVisibility(View.VISIBLE);
                        imageview_jibu.setVisibility(View.GONE);
                        // imageGps.setImageResource(R.drawable.sportmap_gps_3);
                        imageGps.setImageResource(R.drawable.gps_g03);
                    } else if(gpsType == 4){
                        imageGps.setVisibility(View.VISIBLE);
                        imageview_jibu.setVisibility(View.GONE);
                        // imageGps.setImageResource(R.drawable.sportmap_gps_4);
                        imageGps.setImageResource(R.drawable.gps_g04);
                    }else if(gpsType == 5){
                        imageGps.setVisibility(View.VISIBLE);
                        imageview_jibu.setVisibility(View.GONE);
                        // imageGps.setImageResource(R.drawable.sportmap_gps_4);
                        imageGps.setImageResource(R.drawable.gps_g05);
                    }

                    // if (gpsType == 0) {
                    // if (isStart && !isPause && !isPauseForGPS
                    // && !isPauseForClick) {
                    // if (isWaitForPause) {
                    // int rec = (int) ((System.currentTimeMillis() -
                    // waitForPauseTime) / 1000L);
                    // // GPS信号丢失30秒后暂停
                    // if (rec > 30) {
                    // //语音提示
                    // vp=new VoicePrompt(getApplicationContext(), "female",
                    // "loseGPS", null);
                    // vp.playVoice();
                    // goPause();
                    // waitForResume = 5;
                    // isPauseForGPS = true;
                    // isWaitForPause = false;
                    // }
                    // } else {
                    // waitForPauseTime = System.currentTimeMillis();
                    // isWaitForPause = true;
                    // }
                    // }
                    // } else {
                    // if (isWaitForPause)
                    // isWaitForPause = false;
                    // if (!isStart && isPause && isPauseForGPS
                    // && !isPauseForClick) {
                    // if (waitForResume == 0) {
                    // vp=new VoicePrompt(getApplicationContext(), "female",
                    // "obtainGPS", null);
                    // vp.playVoice();
                    // goResume();
                    // isPauseForGPS = false;
                    // waitForResume = 5;
                    // } else {
                    // waitForResume--;
                    // }
                    // }
                    // }
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    // isWaitForPause = false;
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
//                    Toast.makeText(mContext, "定位结束", Toast.LENGTH_SHORT).show();
                    imageGps.setImageResource(R.drawable.gps_g01);

                    // if (isStart && !isPause && !isPauseForGPS &&
                    // !isPauseForClick) {
                    // isWaitForPause = true;
                    // new Thread() {
                    // @Override
                    // public void run() {
                    // long starttime = System.currentTimeMillis();
                    // while (isWaitForPause) {
                    // int rec = (int) ((System.currentTimeMillis() - starttime) /
                    // 1000L);
                    // // GPS信号丢失30秒后暂停
                    // if (rec > 30) {
                    // Message message = new Message();
                    // message.what = GO_PAUSE;
                    // handler.sendMessage(message);
                    // isPauseForGPS = true;
                    // isWaitForPause = false;
                    // }
                    // }
                    // }
                    // }.start();
                    // }
                    break;
            }
        }

        ;
    };

    private void getGPSStatus() {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        float f1 = 0;
        float f2 = 0;

        // 获取卫星颗数的默认最大值
        int maxSatellites = gpsStatus.getMaxSatellites();
        // 创建一个迭代器保存所有卫星
        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
        int count = 0;
        while (iters.hasNext() && count <= maxSatellites) {
            GpsSatellite s = iters.next();
            if (s.getSnr() >=20.0f) {
                f1 += s.getSnr();
                count++;
            }
        }
        if(count!=0){
            f2 = f1 / count;
        }
        gpsType = 0;
        if ((count > 10) && (f2 > 31.0F)) {
            gpsType=5;
        } else if ((count > 8) && (f2 > 28.0F)) {
            gpsType = 4;
        } else if ((count > 6) && (f2 > 25.0F)) {
            gpsType = 3;
        } else if ((count > 5) && (f2 > 22.0F)) {
            gpsType = 2;
        } else if ((count > 3) && (f2 >=20.0F)) {
            gpsType = 1;
        }else{
            gpsType=0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
            CountStep(event.values[0], event.values[1], event.values[2]);
        }
    }

    private void CountStep(float x, float y, float z) {
        accelerometer = (float) Math.sqrt(x * x + y * y + z * z);
        if (accelerometer < 8) {
            mTroughAppear = true;
        }

        if (accelerometer > 9.8) {
            if (mTroughAppear) {
                if (System.currentTimeMillis() - mPrevStepTime > 200) {
                    mCountNum++;
                    if (mCountNum > 10) {
                        gps_type=2;
                        isStepBegin = true;
                        if (typeId == SportsType.TYPE_RUN) {
                            // 跑步
                            dis = dis + (10.0 * 1.05 / 10000);
                            temDis += (10.0 * 1.05 / 10000);//临时存储几步模式距离
                        } else {
                            // 走路爬山
                            dis = dis + (7.0 * 1.05 / 10000);
                            temDis += (7.0 * 1.05 / 10000);//临时存储几步模式距离
                        }

                        double avgSpeed = (dis * 3600) / recLen;
                        Boolean normalRange = SportTaskUtil.getNormalRange(typeId,
                                avgSpeed, recLen);
//                        if (normalRange && !speedOverLimit && dis >= 1
//                                && Math.floor(dis) % mKilometre == 0) {
                        //去掉语音播报控制

                        playDisRound();//按照公里数播报语音
                        if (dis >= 1 && Math.floor(dis) % mKilometre == 0) {
                            mKilometre += 1;
                            // String pace = String.valueOf(paceValue.getText());
                            String pace = String.valueOf(paceStringValue);

//                            hashMap.put("gongli", String.valueOf((int) (dis)));
//                            hashMap.put(
//                                    "yongshi",
//                                    String.valueOf(System.currentTimeMillis()
//                                            - startTimeSeconds));
//                            hashMap.put(
//                                    "yongshi",
//                                    String.valueOf(recLen*1000));
//							hashMap.put("peisu_fen",
//									pace.substring(0, pace.indexOf("'")));
//							hashMap.put(
//									"peisu_miao",
//									pace.substring(pace.indexOf("'") + 1,
//											pace.indexOf("\"")));
                            if (typeId == SportsType.TYPE_WALK
                                    || typeId == SportsType.TYPE_RUN
                                    || typeId == SportsType.TYPE_CYCLING) {
                                //只有跑步，骑行，走路有配速
                                int result = 0;
                                if (savePeisuFirst) {
                                    String gl = String.valueOf((int) (dis));
                                    if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                                        peiSuList1.add(gl);
                                        String tempPeisu=recLen * 1000 + "";
                                        peiSuList2.add(tempPeisu);
                                        peiSuList3.add(tempPeisu);
                                        peiSuList4.add(gps_type+"");
                                        String p1,p2,p3,p4;
                                        p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                                        p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                                        p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                                        p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                                        result = SavePeisu(p1, p2, p3,p4, markCode);
                                        if (result > 0) {
                                            savePeisuFirst = false;
                                        }
                                        p1=null;
                                        p2=null;
                                        p3=null;
                                        p4=null;
                                    }
                                    gl = null;

                                } else {
                                    //数据更新方法
                                    String gl = String.valueOf((int) (dis));
                                    if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                                        peiSuList1.add(gl);
                                        String tempPeisu=recLen * 1000 + "";
                                        peiSuList2.add(tempPeisu);
                                        peiSuList3.add(tempPeisu);
                                        peiSuList4.add(gps_type+"");
                                        String p1,p2,p3,p4;
                                        p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                                        p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                                        p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                                        p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                                        result = updatePeisu(p1, p2, p3,p4, markCode, 0 + "");
                                        p1=null;
                                        p2=null;
                                        p3=null;
                                        p4=null;
                                    }
                                    gl = null;
                                }


                            }
//                            vp = new VoicePrompt(getApplicationContext(), "female",
//                                    "moving", hashMap);
//                            vp.playVoice();
                        }

                        //登山
                        if (dis * 1000 >= 10) {
                            if (typeId == SportsType.TYPE_CLIMBING) {
                                //表示登山的时候高度存本地
                                if (dis >= tempDis+0.02) {
                                    //表示不是原地
                                    saveClimb();
                                }

                            }
                        }

                        String disStringValue = SportTaskUtil.getDoubleNumber(dis);
                        time_disValue.setText(disStringValue);
                        zong_li_txt.setText(disStringValue);

                        if (typeId == SportsType.TYPE_WALK) {
                            con = (int) WalkingUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_RUN) {
                            con = (int) RunningUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_CLIMBING) {
                            con = (int) MountainUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_GOLF) {
                            con = (int) GolfingUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_WALKRACE) {
                            con = (int) WalkingRaceUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_CYCLING) {
                            con = (int) RidingUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_FOOTBALL) {
                            con = (int) FootballUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_ROWING) {
                            con = (int) RowingUtils.getCalories(dis);
                        } else if (typeId == SportsType.TYPE_SWIM) {
                            con = (int) SwimmingUtils.getCalories(dis,
                                    recLen, typeDetailId + 1);
                        } else if (typeId == SportsType.TYPE_TENNIS) {
                            // 网球热量计算
                            con = (int) TennisUtils.getCalories(recLen);
                        } else if (typeId == SportsType.TYPE_BADMINTON) {
                            // 羽毛球热量计算
                            con = (int) BadmintonUtils.getCalories(recLen);
                        }
                        conStringValue = con + "";
                        sporting_sportxiaohao.setText(conStringValue + "Cal");


                        //跟暂停状态一样  不划线  计步模式只计算公里不划线
                        stepPause();
                    } else {
                        isStepBegin = false;
                        temDis=0;
                    }
                    mPrevStepTime = System.currentTimeMillis();
                    mTroughAppear = false;
                }
            }
        }
    }

    private void updateStepInformation() {
        // TODO Auto-generated method stub
        if(recLen>0){
            String avgSpeed=SportTaskUtil.getDoubleOneNum(dis*3600/(recLen));
            sporting_sport_pingjunSpeed.setText(avgSpeed + "km/h");
        }
        if (lastCountNum <= mCountNum) {
            speed = (mCountNum - lastCountNum) * 2.16f;
            speedStringValue = SportTaskUtil.getDoubleOneNum(speed);
            if (lastCountNum < mCountNum) {
                if (speed != 0) {
                    pace = 3600 / speed;
                    otherSpeed = speed;
                } else {
                    if (otherSpeed != 0) {
                        pace = 3600 / otherSpeed;
                    } else {
                        pace = 0;
                    }
                }
            } else {
                if (otherSpeed != 0) {
                    pace = 3600 / otherSpeed;
                } else {
                    pace = 0;
                }
            }

            paceStringValue = (int) (pace / 60) + "'" + (int) (pace % 60)
                    + "\"";
            sporting_sport_peiPace.setText(paceStringValue + "/km");

            lastCountNum = mCountNum;
        }
    }

//	private void showSportWindow() {
//		// if (mSportWindow == null) {
//		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = layoutInflater.inflate(R.layout.sportsing_data_poplayout,
//				null);
//		RoundedImage sports_user_icon = (RoundedImage) view
//				.findViewById(R.id.sports_user_icon);
//		if (SportsApp.getInstance().getSportUser().getSex().equals("man")) {
//			sports_user_icon.setImageResource(R.drawable.sports_residemenu_man);
//		} else {
//			sports_user_icon
//					.setImageResource(R.drawable.sports_residemenu_woman);
//		}
//		mImageWorker_Icon.loadImage(SportsApp.getInstance().getSportUser()
//				.getUimg(), sports_user_icon, null, null, false);
//		TextView weather_data = (TextView) view.findViewById(R.id.weather_data);
//		weather_data.setText(getSharedPreferences("sports", 0).getString(
//				"weather_content", ""));
//		TextView area_data = (TextView) view.findViewById(R.id.area_data);
//		area_data.setText(SportsApp.getInstance().getSportUser().getProvince());
//		TextView sports_level = (TextView) view.findViewById(R.id.sports_level);
//		ImageButton pbigBtn = (ImageButton) view.findViewById(R.id.bigBtn);
//		pop_chronometerId = (TextView) view
//				.findViewById(R.id.pop_chronometerId);
//		disValue = (TextView) view.findViewById(R.id.disValue);
//		if (disStringValue != null && !"".equals(disStringValue)) {
//			disValue.setText(disStringValue);
//		}
//		conValue = (TextView) view.findViewById(R.id.conValue);
//		if (conStringValue != null && !"".equals(conStringValue)) {
//			conValue.setText(conStringValue);
//		}
//		speedValue = (TextView) view.findViewById(R.id.speedValue);
//		if (speedStringValue != null && !"".equals(speedStringValue)) {
//			speedValue.setText(speedStringValue);
//		}
//		paceValue = (TextView) view.findViewById(R.id.paceValue);
//		if (paceStringValue != null && !"".equals(paceStringValue)) {
//			paceValue.setText(paceStringValue);
//		}
//		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//		int width = wm.getDefaultDisplay().getWidth();
//		int popWith = width * 2 / 3;
//		mSportWindow = new PopupWindow(view, popWith, LayoutParams.WRAP_CONTENT);
//		pbigBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				mSportWindow.dismiss();
//				unfoldBtn.setVisibility(View.VISIBLE);
//				pop_chronometerId = null;
//			}
//		});
//
//		// }
//		mSportWindow.setTouchable(true);
//		// mSportWindow.setOutsideTouchable(true);
//		mSportWindow.setTouchInterceptor(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//
//				Log.i("mengdd", "onTouch : ");
//
//				return false;
//				// 这里如果返回true的话，touch事件将被拦截
//				// 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
//			}
//		});
//		mSportWindow.setBackgroundDrawable(new BitmapDrawable());
//		mSportWindow.showAsDropDown(timeLayout, 1, 0);
//
//	}




    //结束运动的方法
    private void endSports(int endtype) {

        FunctionStatic.onPause(
                SportingMapActivityGaode.this,
                FunctionStatic.FUNCTION_START_SPORTS,
                preTime);

        isEndSave = true;
        if (sensorManager != null) {
            if (typeId != SportsType.TYPE_CYCLING) {
                sensorManager
                        .unregisterListener(SportingMapActivityGaode.this);
            }
        }
        if (dis == 0 || con == 0) {
            SportSubTaskDB.getInstance(mContext)
                    .deleteSportByMarkCode(markCode);
            Toast.makeText(
                    mContext,
                    getString(R.string.sports_no_trajectory_to_save),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (typeId == SportsType.TYPE_CYCLING) {
            if (dis * 1000 < 500 || con <= 0) {
                // 骑行的时候小于500米不能保存
                SportSubTaskDB.getInstance(mContext)
                        .deleteSportByMarkCode(markCode);
                Toast.makeText(
                        mContext,
                        getString(R.string.sports_no_trajectory_to_save),
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            if (dis * 1000 < 100 || con <= 0) {
                // 其他方式的时候小于100米不能保存
                SportSubTaskDB.getInstance(mContext)
                        .deleteSportByMarkCode(markCode);
                if (peisuDB == null) {
                    peisuDB = PeisuDB.getmInstance(getApplicationContext());
                }
                String isUploadTemp =peisuDB.selectisUpload(markCode);
                if(isUploadTemp!=null&&!"".equals(isUploadTemp)){
                    peisuDB.deleteSportByMarkCode(markCode);
                    isUploadTemp=null;
                }
                Toast.makeText(
                        mContext,
                        getString(R.string.sports_no_trajectory_to_save),
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (typeId == SportsType.TYPE_WALK ||
                typeId == SportsType.TYPE_RUN ||
                typeId == SportsType.TYPE_CLIMBING) {

            double jibuAvgSpeed = (dis * 3600) / recLen;
            Boolean jibuNormalRange = SportTaskUtil.getNormalRange(typeId,
                    jibuAvgSpeed,
                    recLen);
            if (jibuNormalRange && !speedOverLimit) {
                updateJiBuStep(true);
            } else {
                updateJiBuStep(false);
            }
        }

        if (mSportsApp.isOpenNetwork()
                && mSportsApp.LoginOption) {

            if (timer != null) {
                timer.cancel();
            }
            isStart = false;
            mSportsApp.isStartY = false;
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            isMedia = false;

            double avgSpeed = (dis * 3600) / recLen;
            Boolean normalRange = SportTaskUtil
                    .getNormalRange(typeId, avgSpeed,
                            recLen);
            if (normalRange && !speedOverLimit) {
                if (mUploadDialog != null
                        && !mUploadDialog.isShowing())
                    mUploadDialog.show();
                coins = dis < 1 ? (dis > 0.5 ? 1 : 0)
                        : (int) Math.floor(dis);
//                if ((coins + randomCoins) > 0) {
                if (coins > 0) {
                    hashMap.put(
                            "coins",
                            String.valueOf(coins));
                    vp = new VoicePrompt(
                            getApplicationContext(),
                            "female", "end", hashMap);
                    vp.playVoice();
                }
                if (typeId == SportsType.TYPE_WALK
                        || typeId == SportsType.TYPE_RUN
                        || typeId == SportsType.TYPE_CLIMBING) {
                    stepNum = (int) (dis * 10000 / 7);
                } else {
                    stepNum = 0;
                }
                String typeStr = getString(SportTaskUtil.getTypeName(typeId));

                if (endtype == 1) {
                    findString = typeStr + " " + SportTaskUtil.getDoubleNum(dis) + "公里  so 轻松！ 运动过后，身心舒畅！[高兴]";
                } else if (endtype == 2) {
                    findString = typeStr + " " + SportTaskUtil.getDoubleNum(dis) + "公里  有点小累，给我个鼓励，还能坚持！[微笑]";
                } else if (endtype == 3) {
                    findString = typeStr + " " + SportTaskUtil.getDoubleNum(dis) + "公里  累到cry，满头大汗，坚持下去，并不难！[泪脸]";
                }
                getPeisu();
                uploadSportTask();
            } else {
                Editor editor = foxSportSetting.edit();
                editor.putBoolean("isupload", true);
                editor.commit();

                Toast.makeText(
                        mContext,
                        getString(R.string.sports_beyond_limit),
                        Toast.LENGTH_SHORT).show();
                // 超出身体极限的不上传
                isUpload = 2;
                int res = save(false);
                if (typeId == SportsType.TYPE_WALK || typeId == SportsType.TYPE_RUN
                        || typeId == SportsType.TYPE_CLIMBING) {
                    updateJiBuStep(false);
                }
                if (typeId == SportsType.TYPE_CLIMBING) {
                    updateisSavalocal(markCode, 1 + "");
                }else{
                    if(dis>=1){
                        updateisSavalocal(markCode, 1 + "");
                    }
                }
                finish();
            }

        } else {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            double avgSpeed = (dis * 3600) / recLen;
            Boolean normalRange = SportTaskUtil
                    .getNormalRange(typeId, avgSpeed,
                            recLen);
            //没有网络并且超速
            if(!mSportsApp.isOpenNetwork()&&(!normalRange || speedOverLimit)){
                Editor editor = foxSportSetting.edit();
                editor.putBoolean("isupload", true);
                editor.commit();

                Toast.makeText(
                        mContext,
                        getString(R.string.sports_beyond_limit),
                        Toast.LENGTH_SHORT).show();
                // 超出身体极限的不上传
                isUpload = 2;
                int res = save(false);
                if (typeId == SportsType.TYPE_WALK || typeId == SportsType.TYPE_RUN
                        || typeId == SportsType.TYPE_CLIMBING) {
                    updateJiBuStep(false);
                }
                if (typeId == SportsType.TYPE_CLIMBING) {
                    updateisSavalocal(markCode, 1 + "");
                }else{
                    if(dis>=1){
                        updateisSavalocal(markCode, 1 + "");
                    }
                }
                finish();
            }else{
                Editor editor = foxSportSetting.edit();
                editor.putBoolean("isupload", false);
                editor.commit();
                Editor qEditor = mContext.getSharedPreferences(
                        "qq_health_sprots", 0).edit();
                qEditor.putString(startTime, startTime);
                qEditor.commit();
                if (mSportsApp.LoginOption
                        && !mSportsApp.isOpenNetwork()) {
                    Toast.makeText(
                            mContext,
                            getString(R.string.error_cannot_access_net),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(
                            mContext,
                            getString(R.string.sports_goto_login),
                            Toast.LENGTH_SHORT).show();
                }
                isUpload = -1;
                saveSportTask();
            }

        }
        sporttype.remove("save");
        sporttype.put("save", "yes");
        MobclickAgent.onEventEnd(
                SportingMapActivityGaode.this,
                "uploadtask", sporttype.toString());
    }

//    Dialog mLoadProgressDialog;
//
//    private void showDialog(String messages) {
//        if (mLoadProgressDialog == null) {
//            mLoadProgressDialog = new Dialog(SportingMapActivityGaode.this,
//                    R.style.sports_dialog);
//            LayoutInflater mInflater = getLayoutInflater();
//            View v1 = mInflater.inflate(R.layout.bestgirl_progressdialog, null);
//            message = (TextView) v1.findViewById(R.id.message);
//            message.setText(messages);
//            v1.setMinimumWidth((int) (SportsApp.ScreenWidth * 0.8));
//            mLoadProgressDialog.setContentView(v1);
//            mLoadProgressDialog.setCanceledOnTouchOutside(false);
//        }
//        message.setText(messages);
//        mLoadProgressDialog.show();
//    }

    private Handler mHandler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 20141112:
                    AddFindItem   back = (AddFindItem) msg.obj;
                    if (back != null) {
                        if (back.flag == 0
                                && (back.findId != null && !"".equals(back.findId))) {
                            SendMsgDetail mSendMsgDetail = new SendMsgDetail();
                            mSendMsgDetail.setMethod_str(findString);
                            mSendMsgDetail.setTimes(System.currentTimeMillis());
                            mSendMsgDetail.setFindId(back.findId);
                            mSendMsgDetail.setUrls(back.urls);
                            mSendMsgDetail.setBigurls(back.bigurls);
                            if (back.urls != null) {
                                if (back.urls.length == 1) {
                                    mSendMsgDetail.setWidth(back.width);
                                    mSendMsgDetail.setHeight(back.height);
                                }

                            }
                            mSendMsgDetail.setComeFrom(cityname);
                            if(mSportsApp!=null){
                                mSportsApp.setmSendMsgDetail(mSendMsgDetail);
                            }
                            Bimp.bmp.clear();
                            Bimp.drr.clear();
                            list_bitmap_path_upload.clear();
                            Bimp.max = 0;
                            Looper.getMainLooper();
                        }
                    }

                    break;

                case 20141115:
                    ApiBack aback = (ApiBack) msg.obj;
                    if (aback != null) {
                        if (aback.getFlag() == 1) {
                            if(typeId == SportsType.TYPE_CLIMBING){
                                try {
                                    String gl = SportTaskUtil.getDoubleNumber(dis);
                                    String peisu;
                                    peisu = String.valueOf(climbHeight);
                                    if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                                        String p1,p2,p3,p4;
                                        p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                                        p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                                        p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                                        p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                                        peiSuList1.add(gl);
                                        peiSuList2.add(peisu);
                                        peiSuList3.add(recLen * 1000 + "");
                                        peiSuList4.add(gps_type+"");
                                        updatePeisu(p1, p2, p3,p4, markCode, 1 + "");
                                        p1=null;
                                        p2=null;
                                        p3=null;
                                        p4=null;
                                    }
                                    gl = null;
                                    peisu = null;
                                }catch (Exception e){

                                }
                            }else{
                                updateisSavalocal(markCode, 1 + "");
                            }
                        } else {
                            if(typeId == SportsType.TYPE_CLIMBING){
                                saveClimb();
                            }
                            Toast.makeText(SportingMapActivityGaode.this, "数据上传失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //保存爬山高度数据（和配速公用一个数据库，peisu表示高度这里，time没有用到这里）
    private void saveClimb() {
        tempDis = dis;
        int result = 0;
        if (savePeisuFirst) {
            String gl = SportTaskUtil.getDoubleNumber(dis);
            String peisu;
            peisu = String.valueOf(climbHeight);
            if (peiSuList1 != null && peiSuList2 != null&&peiSuList3 != null) {
                String p1,p2,p3,p4;
                p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                peiSuList1.add(gl);
                peiSuList2.add(peisu);
                peiSuList3.add(recLen * 1000 + "");
                peiSuList4.add(gps_type+"");
                result = SavePeisu(p1, p2, p3,p4, markCode);
                p1=null;
                p2=null;
                p3=null;
                p4=null;
                if (result > 0) {
                    savePeisuFirst = false;
                }
            }
            gl = null;
            peisu = null;

        } else {
            //数据更新方法
            String gl = SportTaskUtil.getDoubleNumber(dis);
            String peisu;
            peisu = String.valueOf(climbHeight);
            if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                String p1,p2,p3,p4;
                p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                peiSuList1.add(gl);
                peiSuList2.add(peisu);
                peiSuList3.add(recLen * 1000 + "");
                peiSuList4.add(gps_type+"");
                result = updatePeisu(p1, p2, p3,p4, markCode, 0 + "");
                p1=null;
                p2=null;
                p3=null;
                p4=null;
            }
            gl = null;
            peisu = null;
        }

    }

    //上传配速的内部类
    private static  class UploadPeisuThread extends Thread{
        WeakReference<SportingMapActivityGaode> mThreadActivityRef;
        private int flag;//1代表配速2代表上传运动秀

        public UploadPeisuThread(SportingMapActivityGaode activity,int flag) {
            mThreadActivityRef = new WeakReference<SportingMapActivityGaode>(activity);
            this.flag=flag;
        }

        @Override
        public void run() {
            super.run();
            if (mThreadActivityRef.get() == null)
                return;
            if (mThreadActivityRef.get() != null){
                if(flag==1){
                    mThreadActivityRef.get().upLoadePeisuDate();
                }else if(flag==2){
                    mThreadActivityRef.get().upLoadeShowDate();
                }
            }
        }
    }

    //上传配速的方法
    private void upLoadePeisuDate(){
        try {
            ApiBack aback = ApiJsonParser.uploadPeisu(mSportsApp.getSessionId(), taskID, "android", "z" + getResources().getString(R.string.config_game_id),
                    mPeiInfo, typeId);
            Message msg = new Message();
            msg.what = 20141115;
            msg.obj = aback;
            mHandler2.sendMessage(msg);
        } catch (ApiSessionOutException e) {
            e.printStackTrace();
        } catch (ApiNetException e) {
            e.printStackTrace();
        }
    }


    //上传运动秀的方法
    private void upLoadeShowDate(){
        try {
            AddFindItem back = ApiJsonParser.addFind(
                    mSportsApp.getSessionId(),
                    findString, cityname,
                    list_bitmap_path_upload,
                    "", "", "", "");
            Message msg = new Message();
            msg.what = 20141112;
            msg.obj = back;
            mHandler2.sendMessage(msg);
        } catch (ApiNetException e) {
            Message msg = new Message();
            msg.what = 20141113;
            msg.obj = e.exceMsg();
            mHandler2.sendMessage(msg);
        }

    }

    public class HomeWatcherReceiver extends BroadcastReceiver{
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(LOG_TAG, "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i(LOG_TAG, "reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
//                    if (mWakeLock != null && !mWakeLock.isHeld()) {
//                        mWakeLock.acquire();
//                    }

                }
                else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    Log.i(LOG_TAG, "long press home key or activity switch");

                }
                else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    Log.i(LOG_TAG, "lock");
                }
                else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    Log.i(LOG_TAG, "assist");
                }

            }

        }
    }


    private void registerHomeKeyReceiver(Context context) {
        homeWatcherReceiver= new HomeWatcherReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(homeWatcherReceiver, homeFilter);
    }

    private  void unregisterHomeKeyReceiver(Context context) {
        if (null != homeWatcherReceiver) {
            if(context!=null){
                context.unregisterReceiver(homeWatcherReceiver);
            }
        }
    }

    /**
     * 上传完运动数据后更新本地配速或者登山数据
     */
    private void saveDBPeisu(ApiBack back){
        if (back != null) {
            if (back.getFlag() == 1) {
                if(typeId == SportsType.TYPE_CLIMBING){
                    try {
                        String gl = SportTaskUtil.getDoubleNumber(dis);
                        String peisu;
                        peisu = String.valueOf(climbHeight);
                        if (peiSuList1 != null && peiSuList2 != null && peiSuList3 != null) {
                            String p1,p2,p3,p4;
                            p1=SportTrajectoryUtilGaode.peiListToString(peiSuList1);
                            p2=SportTrajectoryUtilGaode.peiListToString(peiSuList2);
                            p3=SportTrajectoryUtilGaode.peiListToString(peiSuList3);
                            p4=SportTrajectoryUtilGaode.peiListToString(peiSuList4);
                            peiSuList1.add(gl);
                            peiSuList2.add(peisu);
                            peiSuList3.add(recLen * 1000 + "");
                            peiSuList4.add(gps_type+"");
                            updatePeisu(p1, p2, p3,p4, markCode, 1 + "");
                            p1=null;
                            p2=null;
                            p3=null;
                            p4=null;
                        }
                        gl = null;
                        peisu = null;
                    }catch (Exception e){

                    }
                }else{
                    if(dis>=1&&mPeiInfo!=null&&mPeiInfo.getListpeis()!=null&&mPeiInfo.getListpeis().size()>0){
                        updateisSavalocal(markCode, 1 + "");
                    }
                }
            } else {
                if(typeId == SportsType.TYPE_CLIMBING){
                    saveClimb();
                }
            }
        }

    }

    /**
     * 语音播报频率是按照公里数的情况
     */
    private void playDisRound(){
        if (isVoiceON){
            if(playdis>0){
                if(playDisNum==0){
                    if(dis>=playdis){
                        playDisNum=(((int)dis)/playdis)*playdis+playdis;
                    }else{
                        playDisNum=playdis;
                    }
                }
                //按照公里播报语音
                if (dis >= playdis && Math.floor(dis) % playDisNum == 0) {
//                        if (playTempDis == playDisNum) {
                    playDisNum=playDisNum+playdis;
                    //播报语音
                    hashMap.put("gongli", SportTaskUtil.getDoubleNumber(dis));
                    hashMap.put(
                            "yongshi",
                            String.valueOf(recLen*1000));
                    hashMap.put("type","1");//1表示是正公里播报
                    vp = new VoicePrompt(getApplicationContext(), "female",
                            "moving", hashMap);
                    vp.playVoice();
                }
            }
        }

    }
    /**
     * 语音播报频率是按照时间的情况
     */
    private  void playTimeRound(){
        if (isVoiceON){
            if(playtime>0){
                if(playTimeNum==0){
                    if(recLen>=playtime*60){
                        playTimeNum=(recLen/(playtime*60))*(playtime*60)+(playtime*60);
                    }else{
                        playTimeNum=(playtime*60);
                    }
                }
                //按照分钟播报语音
                if (recLen >= playtime*60 && recLen % playTimeNum == 0) {
//                      if (playTemptime == playTimeNum) {
                    playTimeNum=playTimeNum+(playtime*60);
                    //播报语音
                    hashMap.put("gongli", SportTaskUtil.getDoubleNumber(dis));
                    hashMap.put(
                            "yongshi",
                            String.valueOf(recLen*1000));
                    hashMap.put("type","2");//1表示是正公里播报
                    vp = new VoicePrompt(getApplicationContext(), "female",
                            "moving", hashMap);
                    vp.playVoice();

                }

            }
        }

    }

    /**
     * 更新语音播报频率数值
     */
    private void updateYuyinBobaoNum(){
        if(foxSportSetting!=null){
            isOpen = foxSportSetting.getBoolean("lockScreen", false);
            if (isOpen) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }else{
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }

        }

        if(voiceSportSetting!=null){
            isVoiceON = voiceSportSetting.getBoolean("voiceon", true);
            if(isVoiceON){
                playdis=voiceSportSetting.getInt("playdis",0);
                if(playdis>0){
                    playTempDis=(int)dis;
                    if(playTempDis>=playdis){
                        playDisNum=(playTempDis/playdis)*playdis+playdis;
                    }else{
                        playDisNum=playdis;
                    }
                }
                playtime=voiceSportSetting.getInt("playtime",0);
                if(playtime>0){
                    playTemptime=recLen;
                    if(playTemptime>=playtime*60){
                        playTimeNum=(playTemptime/(playtime*60))*(playtime*60)+(playtime*60);
                    }else{
                        playTimeNum=(playtime*60);
                    }
                }
            }
        }
    }




}
