/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.lang.core.type

import org.intellij.lang.annotations.Language
import org.rust.RsTestBase
import org.rust.fileTreeFromText
import org.rust.ide.presentation.render
import org.rust.lang.core.macros.expandedFrom
import org.rust.lang.core.psi.RsExpr
import org.rust.lang.core.psi.RsMacroCall
import org.rust.lang.core.psi.ext.RsInferenceContextOwner
import org.rust.lang.core.psi.ext.descendantsOfType
import org.rust.lang.core.psi.ext.descendantsWithMacrosOfType
import org.rust.lang.core.psi.ext.macroName
import org.rust.lang.core.types.inference
import org.rust.lang.core.types.selfInferenceResult
import org.rust.lang.core.types.type
import org.rust.lang.utils.Severity

abstract class RsTypificationTestBase : RsTestBase() {
    protected fun testExpr(@Language("Rust") code: String, description: String = "", allowErrors: Boolean = false) {
        InlineFile(code)
        check(description)
        if (!allowErrors) checkNoInferenceErrors()
        checkAllExpressionsTypified()
    }

    protected fun stubOnlyTypeInfer(@Language("Rust") code: String, description: String = "", allowErrors: Boolean = false) {
        val testProject = fileTreeFromText(code)
            .createAndOpenFileWithCaretMarker()

        checkAstNotLoaded { file ->
            !file.path.endsWith(testProject.fileWithCaret)
        }

        check(description)
        if (!allowErrors) checkNoInferenceErrors()
        checkAllExpressionsTypified()
    }

    private fun check(description: String) {
        val (expr, data) = findElementAndDataInEditor<RsExpr>()
        val expectedTypes = data.split("|").map(String::trim)

        val type = expr.type.render(skipUnchangedDefaultTypeArguments = false, useAliasNames = false)
        check(type in expectedTypes) {
            "Type mismatch. Expected one of $expectedTypes, found: $type. $description"
        }
    }

    private fun checkNoInferenceErrors() {
        val errors = myFixture.file.descendantsOfType<RsInferenceContextOwner>().asSequence()
            .flatMap { it.selfInferenceResult.diagnostics.asSequence() }
            .map { it.element to it.prepare() }
            .filter { it.second.severity == Severity.ERROR }
            .toList()
        if (errors.isNotEmpty()) {
            error(
                errors.joinToString("\n", "Detected errors during type inference: \n") {
                    "\tAt `${it.first.text}` (line ${it.first.lineNumber}) " +
                        "${it.second.errorCode?.code} ${it.second.header} | ${it.second.description}"
                }
            )
        }
    }

    private fun checkAllExpressionsTypified() {
        val notTypifiedExprs = myFixture.file.descendantsWithMacrosOfType<RsExpr>()
            .filter { expr ->
                expr.inference?.isExprTypeInferred(expr) == false
            }.filter { expr ->
                expr.expandedFrom?.let { it is RsMacroCall && it.macroName in BUILTIN_MACRO_NAMES } != true
            }
        if (notTypifiedExprs.isNotEmpty()) {
            error(
                notTypifiedExprs.joinToString(
                    "\n",
                    "Some expressions are not typified during type inference: \n",
                    "\nNote: All `RsExpr`s must be typified during type inference"
                ) { "\tAt `${it.text}` (line ${it.lineNumber})" }
            )
        }
    }
}

private val BUILTIN_MACRO_NAMES: List<String> = listOf(
    "env", "option_env", "concat", "line", "column", "file", "stringify", "module_path", "cfg"
)
