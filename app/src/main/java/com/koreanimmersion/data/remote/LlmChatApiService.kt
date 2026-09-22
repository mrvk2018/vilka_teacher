package com.koreanimmersion.data.remote

import com.koreanimmersion.data.remote.dto.LlmChatRequestDto
import com.koreanimmersion.data.remote.dto.LlmChatResponseDto

interface LlmChatApiService {
    suspend fun sendMessage(request: LlmChatRequestDto): LlmChatResponseDto
}
