package com.nawin.booknook.data.account

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.createSupabaseClient

/**
 * The Supabase client for the project at [url], or null when the build has no
 * project configured (no `supabase.url` / `supabase.key` in local.properties).
 *
 * [configure] and [configureAuth] let tests swap the HTTP engine and keep the
 * session in memory instead of in the device settings.
 */
fun supabaseClientOrNull(
    url: String,
    key: String,
    configureAuth: AuthConfig.() -> Unit = {},
    configure: SupabaseClientBuilder.() -> Unit = {},
): SupabaseClient? {
    if (url.isBlank() || key.isBlank()) return null
    return createSupabaseClient(url, key) {
        install(Auth) { configureAuth() }
        configure()
    }
}
