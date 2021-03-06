package com.traderx.api.request

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("iban") val iban: String?
)