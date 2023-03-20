/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.ssr

class RsSSRCallExprTest: RsSSRTestBase() {
    fun `test CallExpr no args`() = doTest("""
        fn foo() {
            /*warning*/foo();/*warning**/
            fooo();
            bar();
        }
    """, """foo()""")

    fun `test CallExpr one arg`() = doTest("""
        fn foo() {
            /*warning*/bar(1);/*warning**/
            /*warning*/bar('a');/*warning**/
            /*warning*/bar(x("y"));/*warning**/
            bar();
            bar(1, 2);
            bar();
            bar('a', true, "");
        }
    """, """'_('_)""")
}
