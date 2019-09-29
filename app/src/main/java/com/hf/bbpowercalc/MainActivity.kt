package com.hf.bbpowercalc

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*

//import com.google.android.gms.ads.MobileAds

import android.content.pm.PackageManager
import android.R.attr.versionCode
import android.R.attr.versionName
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import java.math.RoundingMode
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Build
import android.preference.Preference
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var locationManager : LocationManager? = null

    private var dropDistValue   = 0.5
    private var bbFlightDist    = 0.0
    private var defaultVal      = 0.0          // Temporary value for when swapping input modes

    private var jouleLimit:Double = 0.0
    private var locationName:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        // NOTE: Does not work with Target SDK 28+. Dummied out until supported ad version is released
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        //MobileAds.initialize(this, "ca-app-pub-7564011937299425~1235122750")

        // Establish settings
        //val settings = getSharedPreferences("settings_general", Context.MODE_PRIVATE)

        //val prefs = this.getSharedPreferences("settings_general", Context.MODE_PRIVATE)
        //val lanSettings = prefs.getString("language", null)

        //settings.getString("distance_unit", "<unset>")
        //settings.getString("language", "<unset>")

        // Set location label
        val jouleEntriesArray     = resources.getStringArray(R.array.location_entries)
        val locationCodeArray   = resources.getStringArray(R.array.location_values)
        val jouleValueArray     = resources.getIntArray(R.array.location_jouleValues)

        val settings= PreferenceManager.getDefaultSharedPreferences(this)
        val locationCode    = settings.getString("location", "hk")

        val locationIndex   = locationCodeArray.indexOf(locationCode)
        locationName    = jouleEntriesArray[locationIndex]
        jouleLimit      = jouleValueArray[locationIndex].toDouble()/1000

        lbl_CurrentLocation.text = getString(R.string.locationCurr_string) + locationName

        resetOutputStrings()

        // Generate version number in top menu
        if(android.os.Build.VERSION.SDK_INT < 28) {
            lbl_version.text = "Version "+packageManager.getPackageInfo(packageName, 0).versionName.toString()
        } else {
            lbl_version.text = "Version "+packageManager.getPackageInfo(packageName, 0).versionName.toString()+" ("+packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()+")"
        }
        updateBBDistLabel()

        // Set attributes of BB Drop Tolerance seekbar (slider)
        val seekbar = findViewById<View>(R.id.seekbar_bbDropTolerance) as SeekBar
        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dropDistValue = progress.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()/10 + 0.1

                lbl_output_dropDistVal.text = "%.2f".format(dropDistValue) + getDistMeasure()
                updateBBDistLabel()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
        })
        // Initialize drop distance format
        lbl_output_dropDistVal.text = "%.2f".format(dropDistValue) + getDistMeasure()
        lbl_Slider_0_00m.text = "0.10" + getDistMeasure()
        lbl_Slider_1_00m.text = "1.00" + getDistMeasure()

        // Set language
        //setLanguage(getPreferences("language"))
        //setTheme(R.style.iDroid_AppTheme)
        //setContentView(R.layout.activity_main)
        /*
        var decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
                */
    }
    /*
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
    */

    /*
    fun setLanguage(localeCode:String) {
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(localeCode.toLowerCase()))
        } else {
            conf.locale = Locale(localeCode.toLowerCase())
        }
        res.updateConfiguration(conf,dm)
    }
    */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.item_settings -> {
                //Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SettingsActivity::class.java).apply { }
                startActivity(intent)
                true
            }
            /*
            R.id.item_about -> {
                //Toast.makeText(this, getString(R.string.options_coming_soon_string), Toast.LENGTH_SHORT).show()
                //val intent = Intent(this, About::class.java).apply { }
                startActivity(intent)
                true
            }
            */
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }



    /*
    * Calculation Algorithms
    * Let:
    * m = mass      (kg; for grams: divide by 1000)
    * v = velocity  (meters per second; for feet: multiply by 3.281)
    * j = force     (joules)
    *
    * Basic Equation: Force = Mass x Acceleration
    *
    * Meters per second -> Joules
    * j = (m / 1000) * 0.5 * v^2
    *
    * Feet per second -> Joules
    * J = (m / 1000) * 0.5 * (v * 3.281)^2
    *
    * Joules -> Meters per second
    * v = SquareRoot(J/((0.5 * m)/1000))
    */

    // Constants
    val m2fRate = 3.281                         // Meter to Feet conversion rate
    var currentInputTypeIsVelocity = true;

    fun updateInputType(view:View) {
        var change = false

        if(view is RadioButton) {
            val checked = view.isChecked

            when(view.getId()) {
                R.id.radioBtn_Velocity ->
                    if(!currentInputTypeIsVelocity) {
                        if (checked) {
                            currentInputTypeIsVelocity = true
                            change = true
                            lbl_Target.text = getString(R.string.powerJoules_string)
                        }
                    }
                R.id.radioBtn_Power ->
                    if(currentInputTypeIsVelocity) {
                        if(checked) {
                            currentInputTypeIsVelocity = false
                            change = true
                            lbl_Target.text = getString(R.string.velocity_string)
                        }
                    }
            }
            if(change) {
                resetOutputStrings()
            }
        }
    }

    // Swap input value with default output value (include recalculate process for all values
    private fun resetOutputStrings() {
        //val lblArray = arrayOf(lbl_output_012g, lbl_output_020g, lbl_output_025g, lbl_output_028g)

        // Get value from default value. Change which value to get as reference point later?
        valueText.setText("%.2f".format(defaultVal))

        updateOutputs(defaultVal)               // Update each field with post-conversion value
        lbl_inputUnit.text = getInputUnit()     // Update input value type (m/s, J)
    }
    /*
    private fun resetOutputStrings_V0() {
        val lblArray = arrayOf(lbl_output_012g, lbl_output_020g, lbl_output_025g, lbl_output_028g)

        for(i in lblArray) {
            i.text = "0.00" + getOutputUnit()
            i.setTextColor(getResources().getColor(R.color.colorPrimary))
            //i.setTextColor(R.style.AppTheme)
        }
        lbl_inputUnit.text = getInputUnit()
    }
    */

    private fun getInputUnit():String       {
        if(currentInputTypeIsVelocity) {
            return getSpeedMeasure()
        }
        return getString(R.string.j_string)
    }
    private fun getOutputUnit():String      {
        if(currentInputTypeIsVelocity) {
            return getString(R.string.j_string)
        }
        return getSpeedMeasure()
    }
    private fun getConversionType():String  {
        if(currentInputTypeIsVelocity) {
            return getString(R.string.toJoule_string)
        }
        return getString(R.string.toVelocity_string)
    }

    private fun getSpeedMeasure():String {
        if(getPreferences("distance_unit") == "metric") {
            return getString(R.string.mps_string)
        }
        return getString(R.string.ftps_string)
    }
    private fun getDistMeasure():String {
        if(getPreferences("distance_unit") == "imperial") {
            return "ft"
        }
        return "m"
    }


    // Update all values
    var overrideOccurred = false
    fun updateValues(view:View) {
        var inputValue = 0.0
        var textVal = valueText.text.toString()
        var toastString = ""

        // Catch empty inputs to prevent divide by zero issues
        // Set valueText to default value of 0 if empty
        if(textVal != "") {
            inputValue = textVal.toDouble()

            // Override value with upper limits if needed
            // m/s: 330, ft/s: 1082.68, J = 10.89
            if(currentInputTypeIsVelocity) {
                if(getPreferences("distance_unit") == "imperial") {
                    // Check value for Feet
                    inputValue = checkOverrideInput(inputValue, 1082.68)
                } else {
                    // Check value for Meters
                    inputValue = checkOverrideInput(inputValue, 330.toDouble())
                }
            } else {
                // Check value for Joules
                inputValue = checkOverrideInput(inputValue, 10.89)
            }
        } else {
            valueText.text.insert(0,"0")
        }
        updateOutputs(inputValue)

        // Warn user that entry value is empty, and therefore default value of zero is used
        if(textVal != "") {
            if(overrideOccurred) {
                toastString = getString(R.string.textToast_largeValue_string)
            } else {
                toastString = getString(R.string.textToast_calculationComplete_string)
            }
        } else {
            toastString = getString(R.string.textToast_noEntry)
        }
        Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show()

        updateBBDistLabel()

        // Plot estimated trajectory of BB
        /*
        val initAngle = 5

        // Generate coordinates for distance and

        val graph = findViewById(R.id.graph_trajectory) as GraphView
        val series = LineGraphSeries(arrayOf(
            DataPoint(0.0, 1.0),
            DataPoint(0.1, 5.0),
            DataPoint(2.0, 3.0)
        ))
        series.

        graph.addSeries(series)
        */
    }

    private fun checkOverrideInput(input:Double, overrideValue:Double):Double {
        if(input > overrideValue) {
            valueText.setText(overrideValue.toString())
            overrideOccurred = true
            return overrideValue
        }
        overrideOccurred = false
        return input
    }

    private fun updateOutputs(inputValue:Double) {
        var outputValue = 0.0

        outputValue = calculateValue(inputValue, 0.12, getConversionType());
        lbl_output_012g.text = "%.2f".format(outputValue).toString() + getOutputUnit()
        setTextColor(lbl_output_012g, outputValue)

        outputValue = calculateValue(inputValue, 0.2, getConversionType());
        lbl_output_020g.text = "%.2f".format(outputValue).toString() + getOutputUnit()
        setTextColor(lbl_output_020g, outputValue)
        defaultVal = outputValue

        outputValue = calculateValue(inputValue, 0.25, getConversionType());
        lbl_output_025g.text = "%.2f".format(outputValue).toString() + getOutputUnit()
        setTextColor(lbl_output_025g, outputValue)

        outputValue = calculateValue(inputValue, 0.28, getConversionType());
        lbl_output_028g.text = "%.2f".format(outputValue).toString() + getOutputUnit()
        setTextColor(lbl_output_028g, outputValue)
    }

    // Set estimated flight distance label
    private fun updateBBDistLabel() {
        lbl_output_BBDist.text = "%.2f".format(calculateEffectiveRange(bbFlightDist, dropDistValue)).toString() + getDistMeasure()
    }

    // Set text color of values (if joule ratings exceed safety limit settings)
    private fun setTextColor(textObject:TextView, value:Double) {
        if(currentInputTypeIsVelocity) {
            if (value >= jouleLimit) {
                //ResourcesCompat.getColor(getResources(), R.color.text_warning, null)
                textObject.setTextColor(getResources().getColor(R.color.text_warning))
            } else {
                //ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null)
                textObject.setTextColor(getResources().getColor(R.color.colorPrimary))
            }
        } else {
            //ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null)
            textObject.setTextColor(getResources().getColor(R.color.colorPrimary))
        }
    }

    // Calculate output (To Joules or To Velocity) and return value
    private fun calculateValue(inputValue:Double, mass:Double, type:String):Double {
        var output = 0.0

        var feetMultiplier = 1.0

        if(getPreferences("distance_unit") == "imperial") {
            feetMultiplier = m2fRate
        }

        when(type) {
            "toJoule"       -> output = 0.5 * (mass / 1000) * (((inputValue/feetMultiplier) * (inputValue/feetMultiplier)))     // Velocity to Joules
            "toVelocity"    -> output = Math.sqrt((inputValue)/((0.5 * mass) / 1000)) * feetMultiplier                          // Joules to Velocity
        }

        // Set velocity value
        when(type) {
            "toJoule"       -> bbFlightDist = inputValue
            "toVelocity"    -> bbFlightDist = output
        }

        return output
    }

    // Calculate estimated effective range of projectile and return value in meters
    // Mass irrelevant as gravity is constant
    private fun calculateEffectiveRange(velocity:Double, dropDist:Double):Double {
        /*
        var feetMultiplier = 1.0

        if(getPreferences("distance_unit") == "imperial") {
            feetMultiplier = m2fRate
        }
        */
        // Calculate time before bullet drop of some value (can adjust this)
        // t = sqrt(drop distance / (0.5 * 9.81))
        val timeToDropDist = Math.sqrt(dropDist / (0.5 * 9.81))

        // Calculate horizontal distance traveled before bullet drop dist.
        return (timeToDropDist * (velocity))
    }

    // SETTINGS GET VALUE
    // Accepted values: distance_unit, region, language
    private fun getPreferences(type:String): String {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        return settings.getString(type, "<unset>")
    }





    // DEPRECATED
    /*
    // Convert Values
    fun convertValue() {
        if(lbl_output_020g.text.isNotEmpty()) {
            val inputValue = valueText.text.toString().toFloat()
            val outputValue = (mass / 1000) * 0.5 * (inputValue * inputValue)

            if(outputValue >= 1) {
                lbl_output_020g.setTextColor(Color.parseColor("#FF0000"))
            } else {
                lbl_output_020g.setTextColor(Color.parseColor("#000000"))
            }

            lbl_output_020g.text = "%.4f".format(outputValue).toString() + "J"
        } else {
            // Do nothing
        }
    }
    */

    /*
    fun onRadioButtonClickedBBMass(view:View) {
        if(view is RadioButton) {
            val checked = view.isChecked

            when(view.getId()) {
                R.id.radio_mass_012g ->
                    if(checked) {   mass = 0.12 }
                R.id.radio_mass_020g ->
                    if(checked) {   mass = 0.20 }
                R.id.radio_mass_025g ->
                    if(checked) {   mass = 0.25 }
                R.id.radio_mass_028g ->
                    if(checked) {   mass = 0.28 }
            }
        }
    }
    */
}
