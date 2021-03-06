package com.dev.credbizz.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dev.credbizz.R
import com.dev.credbizz.dbHelper.Constants
import com.dev.credbizz.extras.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_create_organization.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit

class CreateOrganization : AppCompatActivity() {

    // SESSION MANAGER
    lateinit var sessionManager: SessionManager

    // CONTEXT
    internal lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_organization)

        // INIT CONTEXT
        context = this

        // INIT SESSION MANAGER
        sessionManager = SessionManager.getInstance(context)!!


        // START BUTTON CLICK
        btn_start.setOnClickListener {
            sessionManager.orgName = ed_name.text.toString()
            val constant = Constants()
            constant.setOrgname(ed_name.text.toString().trim())
            sessionManager.orgName = ed_name.text.toString().trim()
            //send req to profile controller
            updateProfile(constant.getorgName(), constant.getMobileNum())
            val dashboard = Intent(context, Dashboard::class.java)
            dashboard.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboard)
        }

        tx_terms.paintFlags = tx_terms.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tx_privacy.paintFlags = tx_privacy.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tx_terms.setOnClickListener {
            tx_privacy.performClick()
        }

        tx_privacy.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(Keys.privacyUrl)
            startActivity(i)
        }
    }

    private fun updateProfile(orgname: String, mobileNum: String){
        try {
            if (Utils.isNetworkAvailable(this)){
                val retrofit: Retrofit = RetrofitExtra.normalInstance
                val apis = retrofit.create(RetrofitService::class.java)

                // REQUEST OBJECT
                val reqObj = JsonObject()
                reqObj.addProperty("mobileNumber", mobileNum)
                reqObj.addProperty("orgName", orgname)
                Log.e("reqObj", reqObj.toString())

                // API CALL
                apis.updateProfile(reqObj).enqueue(object : retrofit2.Callback<String> {

                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {

                        try {
                            Log.e("getOtpResp", response.toString())

                            if (response.code() == 200) {

                                val responseString = response.body().toString()
                                Log.e("getOtpResp", responseString)


                                //profile updated

                            } else {
                                try {
                                    val responseError = response.errorBody()?.string()
                                    val gson = Gson()
                                    val adapter = gson.getAdapter(JsonObject::class.java)
                                    if (response.errorBody() != null) {
                                        val registerResponse = adapter.fromJson(responseError)
                                        if (registerResponse.has(Keys.error)) {
                                            // Utils.showAlertCustom(context, registerResponse.get(Keys.error).asString)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            } else {
                Utils.showAlertCustom(context, resources.getString(R.string.no_network_connected))
            }
        } catch (e: Exception){
            e.printStackTrace()
        }

    }



}