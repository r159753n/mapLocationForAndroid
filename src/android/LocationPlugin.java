package com.wanjiang.amaplocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * 高德地图 api http://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
 */
public class LocationPlugin extends CordovaPlugin {

    protected final static String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int ACCESS_LOCATION = 1;

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    private Context context;
    private CallbackContext callbackContext = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        context = this.cordova.getActivity().getApplicationContext();
        mLocationClient = new AMapLocationClient(context);
        mLocationClient.setLocationListener(mLocationListener);
        super.initialize(cordova, webView);
        LOG.e("initialize","插件初始化");
    }

    @Override
    public void onDestroy() {
        LOG.e("onDestroy","插件销毁");
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.e("execute","启动执行定位");
        this.callbackContext = callbackContext;
        if ("getlocation".equals(action.toLowerCase(Locale.CHINA))) {
            if (context.getApplicationInfo().targetSdkVersion < 23) {
                this.getLocation();
            } else {
                boolean access_fine_location = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                boolean access_coarse_location = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (access_fine_location && access_coarse_location) {
                    this.getLocation();
                } else {
                    PermissionHelper.requestPermissions(this, ACCESS_LOCATION, permissions);
                }
            }
            return true;
        }
        return false;
    }

    private void getLocation() {
        mLocationOption = new AMapLocationClientOption();

        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);

//        // 获取一次定位结果：
//        // 该方法默认为false。
//        mLocationOption.setOnceLocation(true);
//
//        // 获取最近3s内精度最高的一次定位结果：
//        // 设置setOnceLocationLatest(boolean
//        // b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean
//        // b)接口也会被设置为true，反之不会，默认为false。
//        mLocationOption.setOnceLocationLatest(true);

        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);

        // 单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(30000);

        mLocationOption.setInterval(10000);

        // 关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);

        mLocationOption
                .setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);

        mLocationClient.setLocationOption(mLocationOption); // 设置定位参数
        // 设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        mLocationClient.stopLocation();
        mLocationClient.startLocation(); // 启动定位
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        callbackContext.sendPluginResult(r);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        LOG.e("onRequestPermissionResult","定位授权结果");
        switch (requestCode) {
            case ACCESS_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.getLocation();
                } else {
                    Toast.makeText(this.cordova.getActivity(), "请开启应用定位权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                int errorCode = amapLocation.getErrorCode();
                if (errorCode == 0) {
                    int locationType = amapLocation.getLocationType();//获取当前定位结果来源 定位类型对照表: http://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type/
                    double latitude = amapLocation.getLatitude();//获取纬度
                    double longitude = amapLocation.getLongitude();//获取经度
                    float accuracy = amapLocation.getAccuracy();//获取精度信息
                    String address = amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    String country = amapLocation.getCountry();//国家信息
                    String province = amapLocation.getProvince();//省信息
                    String city = amapLocation.getCity();//城市信息
                    String district = amapLocation.getDistrict();//城区信息
                    String street = amapLocation.getStreet();//街道信息
                    String streetNum = amapLocation.getStreetNum();//街道门牌号信息
                    String cityCode = amapLocation.getCityCode();//城市编码
                    String adCode = amapLocation.getAdCode();//地区编码
                    String aoiName = amapLocation.getAoiName();//获取当前定位点的AOI信息
                    String floor = amapLocation.getFloor();//获取当前室内定位的楼层
                    int gpsAccuracyStatus = amapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
                    long time = amapLocation.getTime();  // 时间
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("locationType", locationType);
                        jsonObject.put("latitude", latitude);
                        jsonObject.put("longitude", longitude);
                        jsonObject.put("accuracy", accuracy);
                        jsonObject.put("address", address);
                        jsonObject.put("country", country);
                        jsonObject.put("province", province);
                        jsonObject.put("city", city);
                        jsonObject.put("district", district);
                        jsonObject.put("street", street);
                        jsonObject.put("streetNum", streetNum);
                        jsonObject.put("cityCode", cityCode);
                        jsonObject.put("adCode", adCode);
                        jsonObject.put("aoiName", aoiName);
                        jsonObject.put("floor", floor);
                        jsonObject.put("gpsAccuracyStatus", gpsAccuracyStatus);
                        jsonObject.put("time", time);
                        LOG.e("定位成功",jsonObject.toString());
                    } catch (JSONException e) {
                        jsonObject = null;
                        e.printStackTrace();
                    }
                    callbackContext.success(jsonObject);
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    String errorInfo = amapLocation.getErrorInfo();
                    Log.e("AmapError", "location Error, ErrCode:"
                            + errorCode + ", errInfo:"
                            + errorInfo);
                    callbackContext.error(errorInfo);
                }
            }
        }
    };

}
