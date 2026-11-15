package com.tora.yetanotherpomo.domain.model

/**
 * Social media apps Night lock blocks out of the box. Stored by package name, so an app that
 * isn't installed yet is already covered the night it gets installed. Messengers are left out on
 * purpose: Night lock is about feeds, not about being reachable.
 */
object SocialApps {
    val defaultBlocklist: Set<String> = setOf(
        "com.instagram.android", // Instagram
        "com.instagram.barcelona", // Threads
        "com.vkontakte.android", // VK
        "com.twitter.android", // X
        "com.reddit.frontpage", // Reddit
        "com.facebook.katana", // Facebook
        "com.facebook.lite", // Facebook Lite
        "com.zhiliaoapp.musically", // TikTok
        "com.ss.android.ugc.trill", // TikTok (some regions)
        "com.zhiliaoapp.musically.go", // TikTok Lite
        "com.snapchat.android", // Snapchat
        "com.pinterest", // Pinterest
        "com.tumblr", // Tumblr
        "ru.ok.android", // Odnoklassniki
        "xyz.blueskyweb.app", // Bluesky
        "org.joinmastodon.android", // Mastodon
    )

    fun isSocial(packageName: String): Boolean = packageName in defaultBlocklist
}
