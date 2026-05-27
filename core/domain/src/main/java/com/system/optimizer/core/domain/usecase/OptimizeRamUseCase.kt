package com.system.optimizer.core.domain.usecase

import com.system.optimizer.core.common.Result
import com.system.optimizer.core.domain.repository.OptimizationRepository
import javax.inject.Inject

class OptimizeRamUseCase @Inject constructor(
    private val repository: OptimizationRepository
) {
    suspend operator fun invoke(): Result<Long> {
        return repository.optimizeRam()
    }
}
