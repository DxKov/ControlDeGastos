package com.example.controldegastos.core.domain.usecase

/**
 * Base class for all UseCases.
 * Encapsulates the execution logic and handles exceptions using [Result].
 */
abstract class UseCase<in P, out R> {
    
    /**
     * Executes the business logic of the UseCase.
     */
    abstract suspend fun execute(params: P): Result<R>

    /**
     * Helper to execute the UseCase without explicit [execute] call.
     */
    suspend operator fun invoke(params: P): Result<R> = execute(params)
}

/**
 * Convenience version of [UseCase] that takes no parameters.
 */
abstract class NoParamsUseCase<out R> : UseCase<Unit, R>() {
    suspend operator fun invoke(): Result<R> = execute(Unit)
}
