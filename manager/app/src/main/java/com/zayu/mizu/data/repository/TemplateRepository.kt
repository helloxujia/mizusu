package com.zayu.mizu.data.repository

import com.zayu.mizu.data.model.TemplateInfo

interface TemplateRepository {
    suspend fun getTemplates(sync: Boolean): Result<List<TemplateInfo>>
    suspend fun importTemplates(jsonString: String): Result<Unit>
    suspend fun exportTemplates(): Result<String>
    suspend fun getTemplate(id: String): Result<TemplateInfo>
}
