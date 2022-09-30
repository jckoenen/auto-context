package de.joekoe.autocontext.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import de.joekoe.autocontext.AutoContext
import de.joekoe.autocontext.processor.codegen.BuilderFunction
import de.joekoe.autocontext.processor.codegen.generate

class AutoContextVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        super.visitAnnotated(annotated, data)
        val classDecl = annotated as? KSClassDeclaration

        if (classDecl?.classKind != ClassKind.INTERFACE) {
            logger.error(
                "${AutoContext::class} is only applicable to interfaces.",
                annotated
            )
        } else {
            generate(codeGenerator, classDecl, BuilderFunction.ALL)
        }
    }
}
