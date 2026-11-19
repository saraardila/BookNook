package com.nawin.booknook.di

import com.nawin.booknook.data.export.CsvLibraryExporter
import com.nawin.booknook.data.reminder.WorkManagerReminderScheduler
import com.nawin.booknook.domain.service.LibraryExporter
import com.nawin.booknook.domain.service.ReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Platform services behind domain interfaces: the reading reminder and the library export. */
val servicesModule = module {
    single<ReminderScheduler> { WorkManagerReminderScheduler(androidContext()) }
    singleOf(::CsvLibraryExporter) bind LibraryExporter::class
}
