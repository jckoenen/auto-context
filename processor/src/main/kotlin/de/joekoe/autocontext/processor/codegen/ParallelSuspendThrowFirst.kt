package de.joekoe.autocontext.processor.codegen

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock

object ParallelSuspendThrowFirst : BuilderFunction {
    override fun generateBuilderFunction(
        sourceTypeName: String,
        contextType: TypeName,
        implType: TypeSpec
    ): FunSpec.Builder {
        val params = implType.propertySpecs.map {
            it.toLambdaBuilder(prefix = "new", suspending = true)
                .addModifiers(KModifier.CROSSINLINE)
                .build()
        }

        return FunSpec.builder(sourceTypeName)
            .addParameters(params)
            .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
            .returns(contextType)
            .addStatement(
                "return %M { %L }", MemberName("kotlinx.coroutines", "coroutineScope"),
                buildCodeBlock {
                    val async = MemberName("kotlinx.coroutines", "async")
                    add("\n")
                    indent()
                    params.forEach { addStatement("val _%N = %M { %N() }", it.name, async, it.name) }

                    add("%N(\n", implType)
                    indent()
                    params.forEach { addStatement("_%N.await(),", it.name) }
                    unindent()
                    add(")\n")
                    unindent()
                }
            )
    }
}
