/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.ssr

class RsSSRExprTest: RsSSRTestBase() {

    fun `test RsContExpr`() = doTest("""
        fn x() {
            for _ in 0..1 {
                /*warning*/continue;/*warning**/
            }
            'foo: for _ in 0..1 {
                /*warning*/continue 'foo;/*warning**/
            }
        }
    """, """continue""")

    fun `test RsContExpr QUOTE_IDENTIFIER`() = doTest("""
        fn x() {
            for _ in 0..1 {
                continue;
            }
            'foo: for _ in 0..1 {
                /*warning*/continue 'foo;/*warning**/
            }
        }
    """, """continue \'foo""")

    fun `test RsContExpr QUOTE_IDENTIFIER text modifier`() = doTest("""
        fn x() {
            for _ in 0..1 {
                continue;
            }
            'foo: for _ in 0..1 {
                /*warning*/continue 'foo;/*warning**/
            }
            'bar: for _ in 0..1 {
                continue 'bar;
            }
        }
    """, """continue \''_:*[regex(foo)]""")

}
