package de.joekoe.autocontext.processor.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import de.joekoe.autocontext.processor.AutoContextProcessor
import java.time.Instant
import java.util.Locale
import javax.annotation.processing.Generated

fun generate(
    codeGenerator: CodeGenerator,
    declaration: KSClassDeclaration,
    builderFunctions: Set<BuilderFunction>
) = with(declaration) {
    val generatedAnnotation = AnnotationSpec.builder(Generated::class)
        .addMember("value = [%S]", AutoContextProcessor::class.qualifiedName!!)
        .addMember("date = %S", Instant.now().toString())
        .build()

    val ctxName = declaration.suffixedPeer("AutoContext")
    val sourceName = declaration.toClassName().simpleName

    val ctx = generateContextType(generatedAnnotation, ctxName)
    val impl = generateImplementation(generatedAnnotation, ctxName)

    FileSpec.builder(declaration.packageName.asString(), ctxName.simpleName)
        .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember("%S, %S", "LocalVariableName", "RedundantVisibilityModifier")
                .build()
        )
        .addType(ctx)
        .addType(impl)
        .apply {
            builderFunctions.map { it.generateBuilderFunction(sourceName, ctxName, impl) }
                .map { it.addAnnotation(generatedAnnotation).build() }
                .forEach { addFunction(it) }
        }
        .build()
        .writeTo(codeGenerator, Dependencies(true, declaration.containingFile!!))
}

inline fun PropertySpec.toLambdaBuilder(
    prefix: String,
    suspending: Boolean = false,
    typeFn: (TypeName) -> TypeName = { it }
) = ParameterSpec.builder(
    prefix + name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
    LambdaTypeName.get(returnType = typeFn(type)).copy(suspending = suspending)
)

private fun KSClassDeclaration.suffixedPeer(suffix: String): ClassName =
    with(toClassName()) { peerClass(simpleName + suffix) }

private fun KSTypeReference.propertyBuilder() = with(resolve()) {
    val propName = declaration.simpleName.getShortName()
        .replaceFirstChar { it.lowercase() }
    PropertySpec.builder(
        name = propName,
        type = toClassName()
    )
}

private fun KSClassDeclaration.generateContextType(generatedAnnotation: AnnotationSpec, name: ClassName): TypeSpec =
    TypeSpec.interfaceBuilder(name)
        .addSuperinterface(toClassName())
        .addProperties(
            superTypes
                .map(KSTypeReference::propertyBuilder)
                .map(PropertySpec.Builder::build)
                .asIterable()
        )
        .addAnnotation(generatedAnnotation)
        .build()

private fun KSClassDeclaration.generateImplementation(
    generatedAnnotation: AnnotationSpec,
    ctxName: ClassName
): TypeSpec {
    val props = superTypes
        .map { type ->
            type.propertyBuilder()
                .addModifiers(KModifier.OVERRIDE)
                .build()
        }
        .asIterable()

    return TypeSpec.classBuilder(suffixedPeer("AutoImpl"))
        .addModifiers(KModifier.DATA)
        .addSuperinterface(toClassName())
        .addSuperinterface(ctxName)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(props.map { ParameterSpec.builder(it.name, it.type).build() })
                .build()
        )
        .addProperties(
            props.map { it.name to it.toBuilder() }
                .map { (name, prop) -> prop.initializer(name) }
                .map { it.build() }
        )
        .apply { props.forEach { addSuperinterface(it.type, it.name) } }
        .addAnnotation(generatedAnnotation)
        .build()
}
