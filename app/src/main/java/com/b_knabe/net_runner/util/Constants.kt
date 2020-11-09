package com.b_knabe.net_runner.util

import com.b_knabe.net_runner.MainActivity

//Activity context link
lateinit var APP_ACTIVITY: MainActivity

const val URL_REGEX = "(http|https)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"
const val KEY_USER_AGENT = "http.agent"
const val HEADER_KEY_USER_AGENT = "User-Agent"
const val HEADER_KEY_CONTENT_TYPE = "Content-Type"
const val DEFAULT_CONTENT_TYPE = "application/json"
const val BUNDLE_KEY_RESPONSE = "response_key"

const val HIDE_MENU = true