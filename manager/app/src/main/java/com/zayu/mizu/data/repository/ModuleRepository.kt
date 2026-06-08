package com.zayu.mizu.data.repository

import com.zayu.mizu.data.model.Module
import com.zayu.mizu.data.model.ModuleUpdateInfo

interface ModuleRepository {
    suspend fun getModules(): Result<List<Module>>
    suspend fun checkUpdate(module: Module): Result<ModuleUpdateInfo>
}
