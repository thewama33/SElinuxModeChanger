package com.wama.selinuxmodechanger;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;

public class MainActivity extends AppCompatActivity {


    private AdView mAdView;

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOnStart();
    }

    @BindView(R.id.layout_gradientColorPermissive)
    LinearLayout relativeLayoutColor;
    @BindView(R.id.layout_gradientColorEnforce)
    LinearLayout relativeLayoutColor1;

    @BindView(R.id.btnChangeMode)
    CardView btnChangeMode;

    @BindView(R.id.txtSelinuxMode)
    TextView txtSelinuxMode;
    @BindView(R.id.txtDeviceModel)
    TextView txtDeviceModel;
    @BindView(R.id.txtDeviceSDK)
    TextView txtDeviceSDK;
    @BindView(R.id.txtisRooted)
    TextView txtisRooted;
    @BindView(R.id.txtDeviceOS)
    TextView txtDeviceOS;


    private static final String TAG = "ConfigMode";
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MobileAds.initialize(this, CheckConfigMode());


        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3979659090641187/3220155633");

        loadAdds();
        addDeviceInfos();
    }

    @OnClick(R.id.layout_gradientColorPermissive)
    public void ChangeSELinuxModeToPermissive() {

        mRewardedVideoAd.loadAd(CheckConfigMode(), new AdRequest.Builder().build());
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

        if (RootShell.isAccessGiven()) {

            changeSELinuxModePermissive();
            Toasty.success(MainActivity.this, "SELinux Mode: Permissive", Toast.LENGTH_LONG).show();

        }

    }

    @OnClick(R.id.layout_gradientColorEnforce)
    public void ChangeSELinuxModeToEnforce() {

        mRewardedVideoAd.loadAd(CheckConfigMode(), new AdRequest.Builder().build());

        if (RootShell.isAccessGiven()) {

            changeSELinuxModeEnforce();
            Toasty.success(MainActivity.this, "SELinux Mode: Enforcing", Toast.LENGTH_LONG).show();


        }

    }


    private String CheckConfigMode() {

        String releaseMode = "ca-app-pub-3979659090641187~5432467578";
        String debugMode = "ca-app-pub-3940256099942544~3347511713";

        if (BuildConfig.DEBUG) {

            Log.d(TAG, "CheckConfigMode: " + debugMode);
            RootShell.debugMode = true; //ON
            return debugMode;
        } else {
            Log.d(TAG, "CheckConfigMode: " + releaseMode);
            RootShell.debugMode = false; //false
            return releaseMode;
        }
    }

    public boolean isSELinuxEnforcing() {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec("getenforce");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        } catch (Exception e) {
            Log.e(TAG, "OS does not support getenforce");
            // If getenforce is not available to the device, assume the device is not enforcing
            e.printStackTrace();
            return false;
        }
        String response = output.toString();
        if ("Enforcing".equals(response)) {

            txtSelinuxMode.setText(getString(R.string.enforcing));
            txtSelinuxMode.setTextColor(getResources().getColor(R.color.red));
            relativeLayoutColor.setBackground(getResources().getDrawable(R.drawable.red_gradient));
            Log.d(TAG, "enforce: " + isSELinuxEnforcing());

            return true;
        } else if ("Permissive".equals(response)) {

            Log.d(TAG, "permissive: " + isSELinuxEnforcing());
            txtSelinuxMode.setText(getString(R.string.permissive));
            txtSelinuxMode.setTextColor(getResources().getColor(R.color.green));
            relativeLayoutColor.setBackground(getResources().getDrawable(R.drawable.green_gradient));
            return false;
        }
        return true;
    }

    private void changeSELinuxModePermissive() {


        try {

            if (RootShell.isAccessGiven()) {
                 Command command = new Command(0, "setenforce 0") {
                    @Override
                    public void commandOutput(int i, String s) {

                    }

                    @Override
                    public void commandTerminated(int i, String s) {

                    }

                    @Override
                    public void commandCompleted(int i, int i1) {

                        txtSelinuxMode.setText(getString(R.string.permissive));
                        txtSelinuxMode.setTextColor(getResources().getColor(R.color.green));

                    }
                };
                RootShell.getShell(true).add(command);
            }
        } catch (IOException | RootDeniedException | TimeoutException ex) {
            ex.printStackTrace();
        }
    }

    private void changeSELinuxModeEnforce() {
        try {
            if (RootShell.isAccessGiven()) {
                Command command = new Command(0, "setenforce 1") {
                    @Override
                    public void commandOutput(int i, String s) {

                    }

                    @Override
                    public void commandTerminated(int i, String s) {

                    }

                    @Override
                    public void commandCompleted(int i, int i1) {
                        txtSelinuxMode.setText(getString(R.string.enforcing));
                        txtSelinuxMode.setTextColor(getResources().getColor(R.color.red));

                    }
                };
                RootShell.getShell(true).add(command);
            }
        } catch (IOException | RootDeniedException | TimeoutException ex) {
            ex.printStackTrace();
        }
    }
    public void addDeviceInfos() {
        try {
            EasyDeviceMod easyDeviceMod = new EasyDeviceMod(this);

            String deviceModel = easyDeviceMod.getModel();
            String deviceSDK = String.valueOf(easyDeviceMod.getBuildVersionSDK());
            boolean deviceISRooted = easyDeviceMod.isDeviceRooted();
            String deviceOSVersion = easyDeviceMod.getOSVersion();

            txtDeviceModel.setText(deviceModel);
            txtDeviceSDK.setText(deviceSDK);
            txtDeviceOS.setText(deviceOSVersion);

            if (deviceISRooted == true) {
                txtisRooted.setText(getString(R.string.rooted));
                txtisRooted.setTextColor(getResources().getColor(R.color.green));
            } else {
                txtisRooted.setText(getString(R.string.not_rooted));
                txtisRooted.setTextColor(getResources().getColor(R.color.red));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            RootShell.log(e.getMessage());
        }


    }

    public void checkOnStart() {
        loadAdds();
        if (RootShell.isRootAvailable()) {

            if (RootShell.isAccessGiven()) {

                isSELinuxEnforcing();
                Log.d(TAG, "onStart: " + isSELinuxEnforcing());
//                if (isSELinuxEnforcing() == true) {
//
//                    Log.d(TAG, "permissive: " + isSELinuxEnforcing());
//                    txtSelinuxMode.setText(getString(R.string.permissive));
//                    txtSelinuxMode.setTextColor(getResources().getColor(R.color.green));
//                    relativeLayoutColor.setBackground(getResources().getDrawable(R.drawable.green_gradient));
//                } else {
//                    txtSelinuxMode.setText(getString(R.string.enforcing));
//                    txtSelinuxMode.setTextColor(getResources().getColor(R.color.red));
//                    relativeLayoutColor.setBackground(getResources().getDrawable(R.drawable.red_gradient));
//                    Log.d(TAG, "enforce: " + isSELinuxEnforcing());
//                }
            } else {
                btnChangeMode.setEnabled(false);
                Toasty.error(this, "App is not Granted!", Toast.LENGTH_LONG).show();
            }

        } else {

            btnChangeMode.setEnabled(false);
            Toasty.error(this, "Device is not Rooted!", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.refreshMode:
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                checkOnStart();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    public void loadAdds() {


        mAdView = findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.loadAd(adRequest);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mAdView.loadAd(adRequest);
            }

        });
    }

}
