/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.ssr

import com.intellij.dupLocator.util.NodeFilter
import com.intellij.psi.PsiElement
import com.intellij.structuralsearch.impl.matcher.compiler.GlobalCompilingVisitor
import com.intellij.structuralsearch.impl.matcher.handlers.SubstitutionHandler
import com.intellij.structuralsearch.impl.matcher.handlers.TopLevelMatchingHandler
import org.rust.lang.core.psi.RsPathExpr
import org.rust.lang.core.psi.RsRecursiveVisitor
import org.rust.lang.core.psi.RsTypeParameter
import org.rust.lang.core.psi.ext.RsGenericParameter

// TODO: implement WordOptimizer and filters
class RsCompilingVisitor(private val myCompilingVisitor: GlobalCompilingVisitor) : RsRecursiveVisitor() {
    fun compile(topLevelElements: Array<out PsiElement>?) {
        val pattern = myCompilingVisitor.context.pattern
        if (topLevelElements == null) return

        for (element in topLevelElements) {
            element.accept(this)
            pattern.setHandler(element, TopLevelMatchingHandler(pattern.getHandler(element)))
        }
    }

    private fun getHandler(element: PsiElement) = myCompilingVisitor.context.pattern.getHandler(element)

    override fun visitElement(element: PsiElement) {
        myCompilingVisitor.handle(element)
        super.visitElement(element)
    }

    override fun visitPathExpr(o: RsPathExpr) {
        visitElement(o)
        val handler = getHandler(o)
        // accept all
        if (handler is SubstitutionHandler) getHandler(o).filter = NodeFilter { it is PsiElement }
    }

    override fun visitTypeParameter(o: RsTypeParameter) {
        visitElement(o)
        val handler = getHandler(o)
        if (handler is SubstitutionHandler) getHandler(o).filter = NodeFilter { it is RsGenericParameter }
    }
}
