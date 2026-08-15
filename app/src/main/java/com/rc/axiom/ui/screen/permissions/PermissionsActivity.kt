package com.rc.axiom.ui.screen.permissions

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.view.View
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.rc.axiom.R
import com.rc.axiom.databinding.ActivityPermissionBinding
import com.rc.axiom.extensions.hasS
import com.rc.axiom.extensions.hasT
import com.rc.axiom.extensions.resources.primaryColor
import com.rc.axiom.ui.component.base.AbsBaseActivity
import com.rc.axiom.ui.component.views.PermissionView
import com.rc.axiom.ui.screen.MainActivity
import com.rc.axiom.data.local.repository.NetworkRepository
import com.rc.axiom.data.model.network.LoginParams
import com.rc.axiom.data.model.network.ScrobblingService
import org.koin.android.ext.android.inject

/**
 * @author Christians M. A. (rc)
 */
class PermissionsActivity : AbsBaseActivity() {

    private val networkRepository: NetworkRepository by inject()

    private var _binding: ActivityPermissionBinding? = null
    private val binding get() = _binding!!

    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAppTitle()
        setupPermissionsVisibility()
        setupPermissionsOrder()

        // Load existing API Keys
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        binding.geniusClientIdInput.setText(prefs.getString("genius_client_id", ""))
        binding.geniusClientSecretInput.setText(prefs.getString("genius_client_secret", ""))
        binding.geniusTokenInput.setText(prefs.getString("genius_access_token", ""))
        binding.spotifyIdInput.setText(prefs.getString("spotify_client_id", ""))
        binding.spotifySecretInput.setText(prefs.getString("spotify_client_secret", ""))
        binding.listenbrainzTokenInput.setText(prefs.getString("listenbrainz_token_onboard", ""))
        binding.lastfmKeyInput.setText(prefs.getString("lastfm_api_key", ""))
        binding.lastfmSecretInput.setText(prefs.getString("lastfm_api_secret", ""))

        // Nothing OS monospaced typography styling
        val ndotTypeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.ndot57)
        binding.helloLabel.typeface = ndotTypeface
        binding.welcomeLabel.typeface = ndotTypeface
        binding.finish.typeface = ndotTypeface
        binding.geniusClientIdInput.typeface = Typeface.MONOSPACE
        binding.geniusClientSecretInput.typeface = Typeface.MONOSPACE
        binding.geniusTokenInput.typeface = Typeface.MONOSPACE
        binding.spotifyIdInput.typeface = Typeface.MONOSPACE
        binding.spotifySecretInput.typeface = Typeface.MONOSPACE
        binding.listenbrainzTokenInput.typeface = Typeface.MONOSPACE
        binding.lastfmKeyInput.typeface = Typeface.MONOSPACE
        binding.lastfmSecretInput.typeface = Typeface.MONOSPACE

        val statusBindings = listOf(
            Triple(binding.geniusClientIdInput, binding.geniusClientIdStatus, "GENIUS CLIENT ID"),
            Triple(binding.geniusClientSecretInput, binding.geniusClientSecretStatus, "GENIUS CLIENT SECRET"),
            Triple(binding.geniusTokenInput, binding.geniusStatus, "GENIUS ACCESS TOKEN"),
            Triple(binding.spotifyIdInput, binding.spotifyIdStatus, "SPOTIFY CLIENT ID"),
            Triple(binding.spotifySecretInput, binding.spotifySecretStatus, "SPOTIFY CLIENT SECRET"),
            Triple(binding.listenbrainzTokenInput, binding.listenbrainzStatus, "LISTENBRAINZ"),
            Triple(binding.lastfmKeyInput, binding.lastfmKeyStatus, "LAST.FM KEY"),
            Triple(binding.lastfmSecretInput, binding.lastfmSecretStatus, "LAST.FM SECRET")
        )

        for ((input, statusView, prefix) in statusBindings) {
            updateApiStatus(input, statusView, prefix)
            input.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateApiStatus(input, statusView, prefix)
                }
            })
        }

        binding.storageAccess.setButtonOnClickListener {
            requestPermissions()
        }
        if (binding.readImages.isVisible) {
            binding.readImages.setButtonOnClickListener {
                if (!binding.readImages.isGranted() && hasT()) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(READ_MEDIA_IMAGES), PERMISSION_REQUEST)
                }
            }
        }
        if (binding.nearbyDevices.isVisible) {
            binding.nearbyDevices.setButtonOnClickListener {
                if (!binding.nearbyDevices.isGranted() && hasS()) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(BLUETOOTH_CONNECT), BLUETOOTH_PERMISSION_REQUEST)
                }
            }
        }
        if (binding.scheduleExactAlarms.isVisible) {
            binding.scheduleExactAlarms.setButtonOnClickListener {
                if (!binding.scheduleExactAlarms.isGranted() && hasS()) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
        binding.finish.setOnClickListener {
            if (currentStep == 1) {
                currentStep = 2
                binding.stepPermissions.visibility = View.GONE
                binding.stepApiKeys.visibility = View.VISIBLE
                checkPermissions()
            } else {
                val geniusClientId = binding.geniusClientIdInput.text?.toString()?.trim().orEmpty()
                val geniusClientSecret = binding.geniusClientSecretInput.text?.toString()?.trim().orEmpty()
                val geniusToken = binding.geniusTokenInput.text?.toString()?.trim().orEmpty()
                val spotifyId = binding.spotifyIdInput.text?.toString()?.trim().orEmpty()
                val spotifySecret = binding.spotifySecretInput.text?.toString()?.trim().orEmpty()
                val listenBrainzToken = binding.listenbrainzTokenInput.text?.toString()?.trim().orEmpty()
                val lastfmKey = binding.lastfmKeyInput.text?.toString()?.trim().orEmpty()
                val lastfmSecret = binding.lastfmSecretInput.text?.toString()?.trim().orEmpty()

                PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
                    putString("genius_client_id", geniusClientId)
                    putString("genius_client_secret", geniusClientSecret)
                    putString("genius_access_token", geniusToken)
                    putString("spotify_client_id", spotifyId)
                    putString("spotify_client_secret", spotifySecret)
                    putString("listenbrainz_token_onboard", listenBrainzToken)
                    putString("lastfm_api_key", lastfmKey)
                    putString("lastfm_api_secret", lastfmSecret)
                    apply()
                }

                if (listenBrainzToken.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            networkRepository.loginToService(
                                ScrobblingService.ListenBrainz,
                                LoginParams.ListenBrainz(listenBrainzToken)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (hasPermissions()) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentStep == 2) {
                    currentStep = 1
                    binding.stepPermissions.visibility = View.VISIBLE
                    binding.stepApiKeys.visibility = View.GONE
                    checkPermissions()
                } else {
                    finishAffinity()
                    remove()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupAppTitle() {
        val appName = getString(R.string.app_name).trim()
        val styledAppName = SpannableStringBuilder(getString(R.string.welcome_to_x, appName).trim()).apply {
            val appNameIndex = indexOf(appName)
            if (appNameIndex != -1) {
                setSpan(StyleSpan(Typeface.BOLD), appNameIndex, appNameIndex + appName.length, SPAN_INCLUSIVE_INCLUSIVE)
                for (i in appName.indices) {
                    val char = appName[i].lowercaseChar()
                    if (char == 'x' || char == 'o') {
                        setSpan(
                            ForegroundColorSpan(android.graphics.Color.RED),
                            appNameIndex + i,
                            appNameIndex + i + 1,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
        binding.welcomeLabel.text = styledAppName
    }

    private fun setupPermissionsVisibility() {
        binding.readImages.isVisible = hasT()
        binding.nearbyDevices.isVisible = hasS()
        binding.scheduleExactAlarms.isVisible = hasS()
    }

    private fun setupPermissionsOrder() {
        var order = 0
        for (i in 0 until binding.stepPermissions.childCount) {
            val child = binding.stepPermissions.getChildAt(i)
            if (child is PermissionView && child.isVisible) {
                child.setNumber(++order)
            }
        }
    }

    private fun startSettingsActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun updateApiStatus(input: android.widget.EditText, statusView: android.widget.TextView, prefix: String) {
        statusView.typeface = Typeface.MONOSPACE
        val text = input.text?.toString()?.trim().orEmpty()
        statusView.text = if (text.isEmpty()) "[ $prefix: NOT SET ]" else "[ $prefix: OK ]"
    }

    private fun checkPermissions() {
        binding.storageAccess.setGranted(hasPermissions())
        if (hasS()) {
            binding.nearbyDevices.setGranted(hasNearbyDevicesPermission())
            binding.scheduleExactAlarms.setGranted(canScheduleExactAlarms())
        }
        if (hasT()) {
            binding.readImages.setGranted(hasReadImagesPermission())
        }

        if (currentStep == 1) {
            val storageGranted = binding.storageAccess.isGranted()
            val nearbyGranted = !hasS() || binding.nearbyDevices.isGranted()
            val imagesGranted = !hasT() || binding.readImages.isGranted()
            val isEnabled = storageGranted && nearbyGranted && imagesGranted

            binding.finish.isEnabled = isEnabled
            binding.finish.text = "NEXT"
            if (isEnabled) {
                binding.finish.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFF0800"))
                binding.finish.setTextColor(android.graphics.Color.WHITE)
            } else {
                binding.finish.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#22FFFFFF"))
                binding.finish.setTextColor(android.graphics.Color.parseColor("#66FFFFFF"))
            }
        } else {
            binding.finish.isEnabled = true
            binding.finish.text = "GET STARTED"
            binding.finish.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFF0800"))
            binding.finish.setTextColor(android.graphics.Color.WHITE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasNearbyDevicesPermission(): Boolean =
        checkSelfPermission(BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasReadImagesPermission(): Boolean =
        checkSelfPermission(READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.S)
    private fun canScheduleExactAlarms(): Boolean =
        getSystemService<AlarmManager>()?.canScheduleExactAlarms() == true
}