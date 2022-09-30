package de.joekoe.autocontext.processor.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

object SimpleBuilderFunction : BuilderFunction {

    override fun generateBuilderFunction(
        sourceTypeName: String,
        contextType: TypeName,
        implType: TypeSpec
    ): FunSpec.Builder {
        val params = implType.propertySpecs.map { it.toLambdaBuilder(prefix = "new").build() }
        return FunSpec.builder(sourceTypeName)
            .addParameters(params)
            .addModifiers(KModifier.INLINE)
            .returns(contextType)
            .addStatement(
                "return %N(${params.joinToString { "%N()" }})",
                implType,
                *params.toTypedArray()
            )
    }
}
