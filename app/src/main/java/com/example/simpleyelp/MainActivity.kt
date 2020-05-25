package com.example.simpleyelp

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_URL = "https://api.yelp.com/v3/"
private const val TAG = "MainActivity"
private const val API_KEY = "v82-4Fyx6flTE9nNIN_X4jXNxgUww8in1sFUzLFIjjlrSoEhWRT3w0y1XiMtLhEZ0qszqG6fZS5xwKanNUMsetuk25ipejzTqNztyvoUHbcFjym_rKPFDYN9QvbKXnYx"
class MainActivity : AppCompatActivity() {

    private fun isNetworkAvailable(): Boolean? {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)
        if(isNetworkAvailable() == true) {
            yelpService.searchRestaurants("Bearer $API_KEY", "Croissant", "Paris, France").enqueue(object: Callback<YelpSearchResults>{
                override fun onFailure(call: Call<YelpSearchResults>, t: Throwable) {
                    Log.i(TAG, "onFail $t")
                }

                override fun onResponse(call: Call<YelpSearchResults>, response: Response<YelpSearchResults>) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if(body == null){
                        Log.w(TAG, "recieved NULL body response")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

            })
        } else{
            Log.i(TAG, "No Network Connection")
            Log.d(TAG, "We were unable to connect to the internet, Please try again later.")
            println("We were unable to connect to the internet, Please try again later.")
                val dialogBuilder = AlertDialog.Builder(this)

                // set message of alert dialog
                dialogBuilder.setMessage("Unable to connect to the internet, Do you want to close this application ?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Close", DialogInterface.OnClickListener {
                            dialog, id -> finish()
                    })
                    // negative button text and action
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Network Connectivity Issue")
                // show alert dialog
                alert.show()
        }

        //execute as soon as network request returns

    }
}
