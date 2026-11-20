package com.nawin.booknook.di

import com.nawin.booknook.BuildConfig
import com.nawin.booknook.data.account.SupabaseAccountRepository
import com.nawin.booknook.data.account.supabaseClientOrNull
import com.nawin.booknook.domain.repository.AccountRepository
import org.koin.dsl.module

/** The optional reader account, on the Supabase project set in local.properties. */
val accountModule = module {
    single<AccountRepository> {
        SupabaseAccountRepository(supabaseClientOrNull(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY))
    }
}
