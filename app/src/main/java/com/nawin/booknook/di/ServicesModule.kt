package com.nawin.booknook.di

import com.nawin.booknook.data.export.CsvLibraryExporter
import com.nawin.booknook.data.reminder.WorkManagerReminderScheduler
import com.nawin.booknook.data.share.AndroidCardSharer
import com.nawin.booknook.domain.service.CardSharer
import com.nawin.booknook.domain.service.LibraryExporter
import com.nawin.booknook.domain.service.ReminderScheduler
import java.time.Clock
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Platform services behind domain interfaces (the reading reminder, the library export and sharing cards),
 * plus the clock that decides what "today" is, so tests can fix it.
 */
val servicesModule = module {
    single<ReminderScheduler> { WorkManagerReminderScheduler(androidContext()) }
    singleOf(::CsvLibraryExporter) bind LibraryExporter::class
    single<CardSharer> { AndroidCardSharer(androidContext()) }
    single<Clock> { Clock.systemDefaultZone() }
}
