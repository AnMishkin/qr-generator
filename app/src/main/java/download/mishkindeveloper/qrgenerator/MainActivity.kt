package download.mishkindeveloper.qrgenerator

//импорт по проверке отзыва
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import download.mishkindeveloper.qrgenerator.databinding.ActivityMainBinding
import download.mishkindeveloper.qrgenerator.reviewManager.ReviewManager
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    private lateinit var activity: Activity
    private var countAd:Int = 0
    private lateinit var analytics: FirebaseAnalytics

    //по проверке отзыва
    lateinit var mAppUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 100
    private var updateCanceled: String? = null
    private var newAppIsReady: String? = null
    private var updateInstall: String? = null

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private lateinit var textReview : String
    private lateinit var laiterReview : String
    private lateinit var leaveReview : String
    private lateinit var okReview : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_QRCreator)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity = this@MainActivity
        updateCanceled = getString(R.string.update_canceled)
        newAppIsReady = getString(R.string.new_app_is_ready)
        updateInstall = getString(R.string.update_install)

        MobileAds.initialize(this) {}

        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        //mAppUpdateManager.registerListener(installStateUpdatedListener)

        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE, this, RC_APP_UPDATE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
        init()
        ReviewManager(this).checkAndPromptForReview(textReview,laiterReview,leaveReview,okReview)
        MobileAds.initialize(this) {}
        initAds()
        analytics = Firebase.analytics
        setupNav()
        setupActionBarWithNavController(navController)
       binding.bottomNavView.setupWithNavController(navController)
    }

    override fun onStop() {
        mAppUpdateManager.unregisterListener(installStateUpdatedListener)
        super.onStop()
    }
    override fun onResume() {
        super.onResume()

        // Проверка состояния обновления
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // Обновление было загружено, отображаем сообщение
                showCompletedUpdate()
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Процесс обновления был приостановлен, возобновляем его
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        RC_APP_UPDATE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNav() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHost.navController
        navController.setGraph(R.navigation.nav_graph)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    //showAd()
                    showBottomNav()
                    //initAds()
                }
                R.id.historyFragment -> {
                    Log.d("MyLog", "count до нажатия на историю - $countAd")
                    if (countAd == 3) {
                        showAd()
                        countAd = 0
                        initAds()
                    } else {
                        countAd++
                    }
                    Log.d("MyLog", "count после нажатия на историю - $countAd")


                 //showAd()
                    Log.d("MyLog","нажали на историю")
                    showBottomNav()
                    //initAds()
                }
                else -> hideBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        binding.bottomNavView.visibility = View.VISIBLE

    }

    private fun hideBottomNav() {
        binding.bottomNavView.visibility = View.GONE

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Clear the Activity's bundle of the subsidiary fragments' bundles.
        outState.clear()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let {
            val imm: InputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as (InputMethodManager)
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun initAds() {

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            "ca-app-pub-3971991853344828/1558485797",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    mInterstitialAd = null
                    Log.d(TAG, "Failed to load interstitial ad.")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
                initAds()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                initAds()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content: ${adError.message}")
                mInterstitialAd = null
                initAds()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
                initAds()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                initAds()
            }
        }
    }
    private fun showAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        } else {
            initAds()
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    //проверка обновления программы
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            // Handle the update cancellation
            Toast.makeText(this, updateCanceled, Toast.LENGTH_SHORT).show()
        }
    }

    private val installStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // Show the update completion message
                showCompletedUpdate()
            }
        }
    private fun showCompletedUpdate() {
        val snackbar = newAppIsReady?.let {
            Snackbar.make(
                findViewById(android.R.id.content), it,
                Snackbar.LENGTH_INDEFINITE
            )
        }
        snackbar?.setAction(
            updateInstall
        ) { mAppUpdateManager.completeUpdate() }
        snackbar?.show()
    }
    //конец проверки обновления программы

    private fun init() {
        textReview = getString(R.string.text_review)
        laiterReview = getString(R.string.laiter_review)
        leaveReview = getString(R.string.leave_review)
        okReview = getString(R.string.ok_review)
    }

    
}




