package de.joekoe.autocontext.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import de.joekoe.autocontext.AutoContext

class AutoContextProcessor(
    private val logger: KSPLogger,
    private val visitor: AutoContextVisitor
) : SymbolProcessor {
    private companion object {
        val annotationName = AutoContext::class.qualifiedName!!
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Starting $this")
        resolver.getSymbolsWithAnnotation(annotationName)
            .forEach { decl -> visitor.visitAnnotated(decl, Unit) }
        return emptyList()
    }
}
