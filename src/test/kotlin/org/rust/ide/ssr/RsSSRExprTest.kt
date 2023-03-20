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

    fun `test RsBreakExpr`() = doTest("""
        fn x() {
            for _ in 0..1 {
                /*warning*/break;/*warning**/
            }
            'foo: for _ in 0..1 {
                /*warning*/break 'foo;/*warning**/
            }
        }
    """, """break""")

    fun `test RsBreakExpr QUOTE_IDENTIFIER`() = doTest("""
        fn x() {
            for _ in 0..1 {
                break;
            }
            'foo: for _ in 0..1 {
                /*warning*/break 'foo;/*warning**/
            }
        }
    """, """break \'foo""")

    fun `test RsBreakExpr QUOTE_IDENTIFIER text modifier`() = doTest("""
        fn x() {
            for _ in 0..1 {
                break;
            }
            'foo: for _ in 0..1 {
                /*warning*/break 'foo;/*warning**/
            }
            'bar: for _ in 0..1 {
                break 'bar;
            }
        }
    """, """break \''_:*[regex(foo)]""")

    fun `test RsBreakExpr EXPR`() = doTest("""
        fn x() {
            for _ in 0..1 {
                break;
            }
            'foo: loop {
                /*warning*/break 2;/*warning**/
            };
            'foo: loop {
                break 5;
            }
        }
    """, """break 2""")

}
