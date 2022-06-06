package ru.rtuitlab.itlab.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.remote.api.devices.DevicesApi
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.MFSApi
import ru.rtuitlab.itlab.data.remote.api.users.UsersApi
import ru.rtuitlab.itlab.data.repository.DevicesRepository
import ru.rtuitlab.itlab.data.repository.MFSRepository
import ru.rtuitlab.itlab.data.repository.UsersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Singleton
    @Provides
    fun provideRepository(usersApi: UsersApi, responseHandler: ResponseHandler) =
        UsersRepository(usersApi, responseHandler)
    @Singleton
    @Provides
    fun provideDevicesRepository( devicesApi: DevicesApi, responseHandler: ResponseHandler) =
        DevicesRepository(devicesApi, responseHandler)
    @Singleton
    @Provides
    fun provideMFSRepository(mfsApi: MFSApi, responseHandler: ResponseHandler) =
        MFSRepository(mfsApi, responseHandler)
}