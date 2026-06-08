package com.zayu.mizu.data.repository

import com.zayu.mizu.data.model.RepoModule

interface ModuleRepoRepository {
    suspend fun fetchModules(): Result<List<RepoModule>>
}
