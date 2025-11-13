package io.github.zidbrain.kotlin.inject.viewmodel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.zidbrain.kotlin.inject.viewmodel.annotations.InjectViewModel
import java.io.OutputStream

class ViewModelProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val viewModels = resolver.getSymbolsWithAnnotation(InjectViewModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (!viewModels.iterator().hasNext())
            return emptyList()

        val pack = "io.github.zidbrain.kotlin.inject.viewmodel"

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = pack,
            fileName = "ViewModelFactories",
        )

        file.use {
            it += pack + "\n"
        }

        return emptyList()
    }
}

