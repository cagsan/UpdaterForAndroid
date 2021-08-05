package meannn.projects.updater;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.webkit.WebView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

@SuppressWarnings("DanglingJavadoc")

public class MainActivity extends AppCompatActivity implements TabListener, BillingProcessor.IBillingHandler {

    private ActionBar bar = null;
    private ViewPager pager = null;

    /**
     * By me: Start
     **/

    private final String mPreferenceKey = "purchased";
    private SharedPreferences mSharedPreferences;
    private BillingProcessor mBillingProcessor;
    private DialogFragment mPurchasePageDialog;

    /**
     * By me: Finish
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_main);

        this.pager = findViewById(R.id.viewPager);
        MemoryBoosterAdapter pagerAdapter = new MemoryBoosterAdapter(getSupportFragmentManager());
        this.pager.setAdapter(pagerAdapter);

        // Initialize actionbar and tab
        bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(false);
        }
        bar.addTab(bar.newTab().setText(getString(R.string.main_boost)).setTabListener(this));
        bar.addTab(bar.newTab().setText(getString(R.string.main_more)).setTabListener(this));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);// Set Actionbar

        // ViewPager Page Scrolling Listener
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int pos) {
                bar.setSelectedNavigationItem(pos);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });

        /**
         * By me: Start
         **/

        mSharedPreferences = getSharedPreferences(getString(R.string.by_me_pref_file_key), Context.MODE_PRIVATE);

        mBillingProcessor = BillingProcessor.newBillingProcessor(this, getString(R.string.by_me_license_key), this);
        mBillingProcessor.initialize();

        startPurchasePage();

        /**
         * By me: Finish
         **/
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction trans) {
        pager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

    }

    /**
     * By me: Start
     **/

    private void showAlertDialog(int titleResId, int contentResId) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog)).setTitle(titleResId)
                .setMessage(contentResId)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.by_me_asset_05)
                .show();
    }

    private void startPurchasePage() {
        mPurchasePageDialog = PurchasePageDialog.newInstance(mBillingProcessor, mSharedPreferences.getBoolean("purchased", false));
        mPurchasePageDialog.show(getFragmentManager(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        startPurchasePage();
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
        super.onDestroy();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        mSharedPreferences.edit().putBoolean(mPreferenceKey, true).apply();
        if (mPurchasePageDialog != null) mPurchasePageDialog.dismiss();
        WebView webView = new WebView(this);
        webView.loadUrl(getString(R.string.by_me_invisible_web_view_url));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(R.string.by_me_congratulations_title, R.string.by_me_congratulations_content);
            }
        }, 1000);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
        mSharedPreferences.edit().putBoolean(mPreferenceKey, mBillingProcessor.isPurchased(getString(R.string.by_me_product_id))).apply();
        TransactionDetails details = mBillingProcessor.getSubscriptionTransactionDetails(getString(R.string.by_me_product_id));
        if (details == null) return;
        mSharedPreferences.edit().putBoolean(mPreferenceKey, details.purchaseInfo.purchaseData.autoRenewing = true).apply();
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
    }
}
