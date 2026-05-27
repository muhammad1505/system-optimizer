package com.system.optimizer.core.domain.usecase

import com.system.optimizer.core.common.Result
import com.system.optimizer.core.domain.repository.OptimizationRepository
import javax.inject.Inject

class OptimizeBatteryUseCase @Inject constructor(
    private val repository: OptimizationRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.optimizeBattery()
    }
}
