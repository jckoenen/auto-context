package de.joekoe.autocontext.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AutoContextProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = with(environment) {
        AutoContextProcessor(logger, AutoContextVisitor(logger, codeGenerator))
    }
}
