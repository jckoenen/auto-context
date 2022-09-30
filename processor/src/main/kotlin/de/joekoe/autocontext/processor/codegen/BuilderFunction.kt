package de.joekoe.autocontext.processor.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

interface BuilderFunction {
    fun generateBuilderFunction(
        sourceTypeName: String,
        contextType: TypeName,
        implType: TypeSpec
    ): FunSpec.Builder

    companion object {
        val ALL = setOf(SimpleBuilderFunction, ParallelSuspendThrowFirst)
    }
}
