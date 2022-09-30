package de.joekoe.autocontext

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import arrow.fx.coroutines.parZip
import javax.annotation.processing.Generated
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

interface A {
    fun foo()

    companion object : A {
        override fun foo() = Unit
    }
}

interface B {
    fun bar()

    companion object : B {
        override fun bar() = Unit
    }
}

@AutoContext
interface Compound : A, B

val test = runBlocking {
    Compound({ yield(); A }, { yield(); B })
        .also {
            it.foo()
            it.bar()
        }
        .run {
            a.foo()
            b.bar()
        }
}

@Generated
inline fun <Error> CompoundEager(
    crossinline newA: () -> Either<Error, A>,
    crossinline newB: () -> Either<Error, B>
): Either<Error, CompoundAutoContext> =
    either.eager { CompoundAutoImpl(newA().bind(), newB().bind()) }

@Generated
suspend inline fun <Error> CompoundEager(
    crossinline newA: suspend () -> Either<Error, A>,
    crossinline newB: suspend () -> Either<Error, B>
): Either<Error, CompoundAutoContext> = either {
    CompoundAutoImpl(newA().bind(), newB().bind())
}

@Generated
@JvmName("CompoundParallel")
suspend inline fun <Error> CompoundEither(
    crossinline newA: suspend () -> Either<Error, A>,
    crossinline newB: suspend () -> Either<Error, B>
): Either<NonEmptyList<Error>, CompoundAutoContext> = parZip({ newA() }, { newB() }) { ea, eb ->
    ea.fold(
        { e -> eb.fold({ nonEmptyListOf(e, it).left() }, { nonEmptyListOf(e).left() }) },
        { a -> eb.fold({ nonEmptyListOf(it).left() }, { CompoundAutoImpl(a, it).right() }) }
    )
}

@Generated
suspend inline fun CompoundParallelThrow(
    crossinline newA: suspend () -> A,
    crossinline newB: suspend () -> B
): Either<NonEmptyList<Throwable>, CompoundAutoContext> =
    parZip({ Either.catch { newA() } }, { Either.catch { newB() } }) { ea, eb ->
        ea.fold(
            { e -> eb.fold({ nonEmptyListOf(e, it).left() }, { nonEmptyListOf(e).left() }) },
            { a -> eb.fold({ nonEmptyListOf(it).left() }, { CompoundAutoImpl(a, it).right() }) }
        )
    }
