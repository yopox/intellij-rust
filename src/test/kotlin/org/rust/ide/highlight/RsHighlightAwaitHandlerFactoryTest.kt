/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.util.ThrowableRunnable
import org.intellij.lang.annotations.Language
import org.rust.RsTestBase
import org.rust.ide.disableFindUsageTests

class RsHighlightAwaitHandlerFactoryTest : RsTestBase() {

    override fun runTestRunnable(testRunnable: ThrowableRunnable<Throwable>) {
        if (!disableFindUsageTests) {
            super.runTestRunnable(testRunnable)
        }
    }

    fun `test highlight await in async function when caret is on async`() = doTest("""
        async fn foo() {}
        struct Bar { bar: i32 }
        /*caret*/async fn test(bar: Bar) {
            foo().await;
            bar.bar;
            if true {
                foo().await;
            }
            async {
                foo().await;
                foo().await;
            };
        }
    """, 4, 7)

    fun `test highlight await in async function when caret is on await`() = doTest("""
        async fn foo() {}
        async fn test(bar: Bar) {
            foo()./*caret*/await;
            foo().await;
        }
    """, 3, 4)

    fun `test highlight await in async block when caret is on async`() = doTest("""
        async fn foo() {}
        fn test() {
            /*caret*/async {
                foo().await;
                foo().await;
            };
            async {
                foo().await;
                foo().await;
            };
        }
    """, 4, 5)

    fun `test highlight await in async block when caret is on await`() = doTest("""
        async fn foo() {}
        fn test() {
            async {
                foo().await;
                foo().await;
            };
            async {
                foo().await/*caret*/;
                foo().await;
            };
        }
    """, 8, 9)

    fun `test highlight chained await`() = doTest("""
        pub as/*caret*/ync fn runner() {
            GreatGrandparent()
                .await
                .parent()
                .await
                .child()
                .await;
        }

        pub async fn GreatGrandparent() -> AsyncGrandparent {
            AsyncGrandparent
        }

        struct AsyncGrandparent;
        impl AsyncGrandparent {
            pub async fn parent(self) -> AsyncParent {
                AsyncParent
            }
        }

        struct AsyncParent;
        impl AsyncParent {
            pub async fn child(self) -> AsyncChild {
                AsyncChild
            }
        }

        struct AsyncChild;
        impl AsyncChild {
            pub async fn foo(self) -> i32 { 0 }
        }
    """, 3, 5, 7)

    private fun doTest(@Language("Rust") check: String, vararg lines: Int) {
        InlineFile(check)
        HighlightUsagesHandler.invoke(myFixture.project, myFixture.editor, myFixture.file)
        val highlighters = myFixture.editor.markupModel.allHighlighters
        val file = myFixture.file
        val actual = highlighters.map {
            file.findElementAt(it.startOffset)!!.lineNumber to file.text.substring(it.startOffset, it.endOffset)
        }
        assertSameElements(actual, lines.map { it to "await" })
    }
}
